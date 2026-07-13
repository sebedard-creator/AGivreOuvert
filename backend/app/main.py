from fastapi import FastAPI, Depends, HTTPException
from sqlmodel import Session, select
from typing import List, Dict, Any
from datetime import date

import random

from dotenv import load_dotenv
load_dotenv()

from .database import create_db_and_tables, get_session
from .models import InventoryItem, KnownProduct
from .services.open_food_facts import lookup_barcode
from .services.recipes import generate_recipes_ai

# Initialisation de FastAPI
app = FastAPI(
    title="À Givre Ouvert API",
    description="API pour la gestion de l'inventaire du congélateur avec rotation FIFO.",
    version="1.0.0"
)

@app.on_event("startup")
def on_startup():
    create_db_and_tables()

# --- ROUTES D'INVENTAIRE ---

@app.get("/api/inventory", response_model=List[InventoryItem])
def get_inventory(session: Session = Depends(get_session)):
    """Retourne tout l'inventaire actif, trié par date_added (plus vieux au plus récent - FIFO)."""
    statement = select(InventoryItem).order_by(InventoryItem.date_added)
    results = session.exec(statement).all()
    return results

@app.post("/api/inventory/add", response_model=InventoryItem)
def add_item(item: InventoryItem, session: Session = Depends(get_session)):
    """Ajoute un nouveau lot à l'inventaire."""
    # Correction Pydantic v2 / SQLModel SQLite
    if isinstance(item.date_added, str):
        item.date_added = date.fromisoformat(item.date_added)
    session.add(item)
    
    # Save or update in dictionary if upc is provided
    if item.upc:
        known = session.get(KnownProduct, item.upc)
        if not known:
            session.add(KnownProduct(upc=item.upc, name=item.name))
        elif known.name != item.name:
            known.name = item.name
            session.add(known)
            
    session.commit()
    session.refresh(item)
    return item

@app.delete("/api/inventory/item/{item_id}", response_model=dict)
def remove_item(item_id: int, session: Session = Depends(get_session)):
    """Retire (consomme) un lot spécifique de l'inventaire via son ID."""
    item = session.get(InventoryItem, item_id)
    if not item:
        raise HTTPException(status_code=404, detail="Item non trouvé")
    
    session.delete(item)
    session.commit()
    return {"status": "success", "message": f"Lot {item_id} retiré de l'inventaire."}

# --- ROUTE SCANNER / OFF ---

@app.get("/api/scanner/lookup/{upc}", response_model=Dict[str, Any])
async def scan_lookup(upc: str, session: Session = Depends(get_session)):
    """
    Recherche le code UPC dans Open Food Facts.
    Vérifie également si des lots locaux existent pour le mode retrait.
    """
    # 1. Vérification des lots locaux (pour le retrait FIFO)
    statement = select(InventoryItem).where(InventoryItem.upc == upc).order_by(InventoryItem.date_added)
    local_lots = session.exec(statement).all()
    
    exists_in_database = len(local_lots) > 0
    
    # 2. Vérification dans le dictionnaire local
    known_product = session.get(KnownProduct, upc)
    
    off_product_name = None
    if known_product:
        off_product_name = known_product.name
    else:
        # 3. Recherche Open Food Facts
        off_data = await lookup_barcode(upc)
        if off_data:
            off_product_name = off_data["name"]
    
    response = {
        "upc": upc,
        "exists_in_database": exists_in_database,
        "off_product_name": off_product_name,
        "local_lots": [
            {
                "id": lot.id,
                "date_added": lot.date_added.isoformat()
            } for lot in local_lots
        ]
    }
    return response

# --- ROUTE RECETTES IA ---

@app.get("/api/recipes/suggestions", response_model=List[Dict[str, Any]])
async def get_recipes_suggestions(session: Session = Depends(get_session)):
    """
    Génère des suggestions de recettes basées sur 10 produits aléatoires parmi les 15 plus anciens.
    """
    statement = select(InventoryItem).order_by(InventoryItem.date_added).limit(15)
    oldest_items = session.exec(statement).all()
    
    if not oldest_items:
        return []
        
    if len(oldest_items) > 10:
        oldest_items = random.sample(list(oldest_items), 10)
        
    items_dicts = [{"name": item.name, "date_added": item.date_added.isoformat()} for item in oldest_items]
    
    # Appel à l'IA Gemini
    recipes = await generate_recipes_ai(items_dicts)
    return recipes
