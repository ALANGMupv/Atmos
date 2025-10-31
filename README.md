# 🌱 Proyecto Biometría 2025  

🔗 **Web del proyecto (Plesk):** [https://nagufor.upv.edu.es/cliente/index.php](https://nagufor.upv.edu.es/cliente/index.php)  

Repositorio del proyecto de **Biometría y Medio Ambiente**. Incluye código, diseños, API REST, base de datos y pruebas automatizadas.  

---

## 📦 Contenido  

- 📂 **Java (Android):** escaneo y procesamiento BLE / iBeacon  
- 📂 **C++ (Arduino):** emisión de beacons y sensores simulados  
- 📂 **Node.js (Backend REST):** servidor Express + conexión MySQL  
- 📂 **Tests (Mocha + Request):** validación de la API y lógica de negocio  
- 🖼️ **Diseños UML y mockups:** diagramas de clases, funciones y frontend  

---

## 🛠️ Diseños  

- [🎨 Java (Ingeniería inversa)](https://www.figma.com/board/5gclkZaVUzniiwrtNu5U6k/Ingenier%C3%ADa-Inversa-Java--Dise%C3%B1o-?node-id=0-1&t=2sPA1xDThr6iYJ2P-1)  
- [🎨 C++ (Ingeniería inversa)](https://www.figma.com/board/M5GoSHtcgihRU9eOloRw9p/Ingenier%C3%ADa-Inversa-C----Dise%C3%B1o-?node-id=0-1&t=fQN68Fkj8KLjSxMq-1)  
- [🎨 Backend y Frontend](https://www.figma.com/board/OhXGiBzKXEmR9dCqy3RwvB/Dise%C3%B1o-Backend-y-Frontend?node-id=0-1&t=462ogik28GMpr2IN-1)  

---

## 🚀 Tecnologías  

- **Java / Android Studio** – app móvil para escaneo BLE  
- **C++ / Arduino IDE** – sensores y emisión de datos  
- **Node.js + Express** – servidor API REST  
- **MySQL (Plesk)** – base de datos para persistencia de medidas  
- **Mocha + Request** – framework de testing automatizado  
- **Figma** – prototipado de diseños  
- **GitHub & Trello** – gestión de proyecto  

---

## 🚀 Despliegue del Proyecto

El proyecto está desplegado en **Plesk (UPV)**:

1. **Backend (Node.js + Express)**  
   - Se ejecuta en el panel de Plesk, con puerto asignado internamente.  

2. **Base de Datos (MySQL en Plesk)**  
   - Configurada y gestionada desde el propio panel.  
   - La API REST se conecta con credenciales locales (`localhost`).  

3. **Frontend (PHP en /httpdocs)**  
   - Desplegado en la carpeta pública de Plesk.  
   - Accesible en:  
     👉 [https://nagufor.upv.edu.es/cliente/index.php](https://nagufor.upv.edu.es/cliente/index.php)

---

## 🧪 Ejecución de Tests

Los tests están desarrollados con **Mocha + Request** y verifican:

- `guardarMedida()` → Inserta una nueva medida y devuelve el registro insertado.  
- `listarMedidas()` → Lista las últimas medidas con límite configurable.  
- `guardarUsuario()` → Crea un usuario nuevo con contraseña cifrada mediante bcrypt.  
- `actualizarEstadoVerificado()` → Marca a un usuario como verificado.  
- `buscarUsuarioPorEmail()` → Busca un usuario existente por su correo electrónico.  
- `actualizarUsuario()` → Actualiza los datos personales de un usuario existente.  
- `obtenerUsuarioPorId()` → Devuelve los datos completos de un usuario o `null` si no existe.  

Para ejecutarlos:

```bash
npm install   # instalar dependencias
npm test      # ejecutar suite de pruebas

