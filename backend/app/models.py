from sqlmodel import SQLModel, Field
from typing import Optional
from datetime import date

class InventoryItem(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str = Field(index=True)
    upc: Optional[str] = Field(default=None, index=True)
    date_added: date

class KnownProduct(SQLModel, table=True):
    upc: str = Field(primary_key=True)
    name: str
