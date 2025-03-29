from fastapi import FastAPI
from motor.motor_asyncio import AsyncIOMotorClient
import os
from pydantic import BaseModel  # Asegúrate de importarlo
from typing import List
from fastapi import HTTPException  # Asegúrate de importarlo


app = FastAPI()

# Conexión a MongoDB Atlas
MONGO_URI = "mongodb+srv://diana:MarielMedina2003@cluster0.ylbjgll.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"
client = AsyncIOMotorClient(MONGO_URI)
db = client.miDB

# ✅ Modelo de eventos
class Evento(BaseModel):
    titulo: str
    hora: str
    ubicacion: str

# ✅ Modelo para agrupar eventos bajo un timestamp
class TimestampEventos(BaseModel):
    timestamp: str
    eventos: List[Evento]

# ✅ Endpoint para obtener todos los eventos por fecha (timestamp)
@app.get("/eventos/{timestamp}")
async def obtener_eventos_por_fecha(timestamp: str):
    resultado = await db.eventos.find_one({"timestamp": timestamp})
    if not resultado:
        raise HTTPException(status_code=404, detail="No hay eventos para esta fecha")
    return resultado

# ✅ Endpoint para agregar eventos bajo un timestamp
@app.post("/eventos/")
async def agregar_eventos(datos: TimestampEventos):
    # Verificamos si ya existe ese timestamp
    existe = await db.eventos.find_one({"timestamp": datos.timestamp})
    
    if existe:
        # Si ya existe, agregamos los nuevos eventos a la lista
        await db.eventos.update_one(
            {"timestamp": datos.timestamp},
            {"$push": {"eventos": {"$each": [evento.dict() for evento in datos.eventos]}}}
        )
    else:
        # Si no existe, creamos un nuevo documento
        await db.eventos.insert_one(datos.dict())
    
    return {"mensaje": "Eventos agregados correctamente"}
