import httpx
from typing import Optional, Dict

OFF_API_URL = "https://world.openfoodfacts.org/api/v0/product/{}.json"

async def lookup_barcode(upc: str) -> Optional[Dict]:
    """
    Interroge l'API Open Food Facts pour un code-barres donné.
    Retourne un dictionnaire avec le nom si trouvé, sinon None.
    """
    url = OFF_API_URL.format(upc)
    headers = {
        "User-Agent": "CongeloInventaire - Android/Python - Version 1.0 - Usage Personnel"
    }
    
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(url, headers=headers, timeout=5.0)
            if response.status_code == 200:
                data = response.json()
                if data.get("status") == 1:
                    product = data.get("product", {})
                    name = product.get("product_name_fr") or product.get("product_name") or "Produit sans nom"
                    
                    return {
                        "name": name
                    }
        except Exception as e:
            print(f"Erreur lors de l'appel OFF API: {e}")
            
    return None
