import os
import json
from typing import List, Dict
from google import genai

async def generate_recipes_ai(items: List[Dict]) -> List[Dict]:
    """
    Générateur de recettes utilisant l'IA Google Gemini.
    """
    if not items:
        return []
        
    api_key = os.environ.get("GEMINI_API_KEY")
    if not api_key or api_key == "VOTRE_CLE_ICI":
        # Fallback si la clé n'est pas configurée
        return [{
            "title": "API Gemini Non Configurée",
            "target": "Configuration requise",
            "freezer": [],
            "fresh": [],
            "steps": "Veuillez ajouter votre clé GEMINI_API_KEY dans le fichier .env du backend pour activer l'IA."
        }]

    # Préparation de la liste des ingrédients
    ingredients_str = ", ".join([f"{item.get('name')} (Lot du {item.get('date_added')})" for item in items])
    
    prompt = f"""
Tu es un chef cuisinier anti-gaspillage. Voici une liste des ingrédients les plus anciens de mon congélateur : {ingredients_str}. 
Ton but est d'utiliser ces ingrédients pour éviter qu'ils ne périment. Génère 10 recettes simples, rapides et créatives. 

CRITIQUE : Il n'est absolument pas nécessaire d'utiliser tous les ingrédients dans une même recette. Chaque recette peut mettre en vedette un seul ingrédient de la liste, ou en combiner quelques-uns. L'objectif est de donner 10 idées indépendantes. 

Formate ta réponse STRICTEMENT en JSON avec la structure exacte suivante (une liste d'objets) sans aucun markdown autour, juste le JSON brut :
[
  {{
    "title": "Nom de la recette",
    "target": "L'ingrédient principal utilisé",
    "freezer": ["Liste des ingrédients du congélateur utilisés dans cette recette"],
    "fresh": ["Liste des ingrédients frais à ajouter"],
    "steps": "Instructions détaillées étape par étape."
  }}
]
    """

    try:
        client = genai.Client(api_key=api_key)
        response = client.models.generate_content(
            model='gemini-flash-lite-latest',
            contents=prompt,
        )
        
        # Nettoyage du JSON renvoyé par l'IA (enlever les éventuels backticks markdown)
        json_text = response.text.strip()
        if json_text.startswith("```json"):
            json_text = json_text[7:]
        if json_text.endswith("```"):
            json_text = json_text[:-3]
            
        recipes = json.loads(json_text.strip())
        return recipes
        
    except Exception as e:
        print(f"Erreur lors de la génération avec Gemini: {e}")
        return [{
            "title": "Erreur de Génération",
            "target": "Erreur",
            "freezer": [],
            "fresh": [],
            "steps": f"L'intelligence artificielle a rencontré un problème : {str(e)}"
        }]
