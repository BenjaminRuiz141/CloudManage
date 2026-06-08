# Endpoints API - CloudManage

Esta guía detalla cómo utilizar los endpoints de la aplicación mediante **Postman**.

## Gestión de Guías (`/guias`)

### 1. Crear Guía de Despacho
Genera una nueva guía, crea el PDF y lo almacena en EFS.
- **Método:** `POST`
- **URL:** `http://localhost:8080/guias`
- **Body (JSON):**
  ```json
  {
      "transportista": "Transportes ABC"
  }
  ```
- **Nota:** Copia el `id` (UUID) de la respuesta para las siguientes operaciones.

### 2. Buscar Guía por Número
Utiliza este endpoint para obtener el `id` (UUID) si solo tienes el número de guía (ej: `G-123...`).
- **Método:** `GET`
- **URL:** `http://localhost:8080/guias/buscar?numeroGuia=G-1780876961919`

### 3. Subir Guía a S3
Sube el archivo PDF desde EFS hacia el bucket de AWS S3.
- **Método:** `POST`
- **URL:** `http://localhost:8080/guias/{id}/upload`
- **Ejemplo:** `http://localhost:8080/guias/59258c2a-1317-4195-a07d-c0751d6ed999/upload`

### 4. Descargar Guía desde S3
Descarga el PDF validando que el solicitante sea el transportista asignado.
- **Método:** `GET`
- **URL:** `http://localhost:8080/guias/{id}/download?transportista={nombre}`
- **Ejemplo:** `http://localhost:8080/guias/59258c2a-1317-4195-a07d-c0751d6ed999/download?transportista=Transportes ABC`

### 5. Actualizar Guía
Modifica los datos de la guía y regenera el PDF en S3.
- **Método:** `PUT`
- **URL:** `http://localhost:8080/guias/{id}`
- **Body (JSON):**
  ```json
  {
      "transportista": "Transportes XYZ"
  }
  ```

### 6. Eliminar Guía
Elimina el objeto de S3 y marca la guía como eliminada en la DB.
- **Método:** `DELETE`
- **URL:** `http://localhost:8080/guias/{id}`

### 7. Consultar Historial
Lista guías con filtros opcionales.
- **Método:** `GET`
- **URL:** `http://localhost:8080/guias`
- **Parámetros opcionales:**
  - `transportista`: Nombre del transportista.
  - `fecha`: Fecha en formato `YYYY-MM-DD`.
- **Ejemplo:** `http://localhost:8080/guias?transportista=Transportes ABC&fecha=2026-06-07`

---
