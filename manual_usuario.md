# 📘 Manual de Usuario - Proyecto Atmos2 (Biometría 2025)

Bienvenido al manual de usuario del proyecto **Atmos2**. Este sistema permite la monitorización de parámetros ambientales (como temperatura y niveles de ozono) mediante sensores móviles, y la visualización de estos datos tanto en una aplicación móvil como en un portal web.

---

## 📋 Índice
1. [Introducción](#1-introducción)
2. [Instalación y Requisitos](#2-instalación-y-requisitos)
3. [Aplicación Móvil (Android)](#3-aplicación-móvil-android)
   - [Inicio de Sesión y Registro](#31-inicio-de-sesión-y-registro)
   - [Menú Principal](#32-menú-principal)
   - [Escaneo de Sensores](#33-escaneo-de-sensores)
   - [Mapa de Contaminación](#34-mapa-de-contaminación)
   - [Gestión de Incidencias](#35-gestión-de-incidencias)
4. [Portal Web](#4-portal-web)
   - [Acceso y Dashboard](#41-acceso-y-dashboard)
   - [Gestión de Perfil](#42-gestión-de-perfil)
5. [Hardware (Sensores)](#5-hardware-sensores)

---

## 1. Introducción
El sistema se compone de tres partes fundamentales:
- **Sensores (Dispositivos):** Pequeños dispositivos basados en Arduino/ESP que emiten datos ambientales vía Bluetooth (BLE).
- **Aplicación Android:** Recoge los datos de los sensores cercanos, permite visualizar mapas y gestionar incidencias.
- **Portal Web:** Plataforma centralizada para consultar históricos, mapas globales y administrar cuentas de usuario.

---

## 2. Instalación y Requisitos

### Requisitos Android
- Dispositivo con **Android 8.0** o superior.
- Conexión **Bluetooth** y **GPS** activados (para la localización de medidas).
- Conexión a Internet (para sincronizar datos con el servidor).

### Acceso Web
- Cualquier navegador moderno (Chrome, Firefox, Safari, Edge).
- Dirección oficial: [https://nagufor.upv.edu.es/cliente/index.php](https://nagufor.upv.edu.es/cliente/index.php)

---

## 3. Aplicación Móvil (Android)

### 3.1 Inicio de Sesión y Registro
Al abrir la aplicación por primera vez, verás la pantalla de bienvenida.
- **Registro:** Si no tienes cuenta, selecciona la opción de registrarse. Deberás proporcionar un correo electrónico válido y crear una contraseña.
- **Login:** Introduce tus credenciales. Si olvidas tu contraseña, usa la opción "¿Has olvidado la contraseña?" para restablecerla vía email.

### 3.2 Menú Principal
Desde el menú principal puedes acceder a todas las funciones:
- **Mapas:** Ver la situación ambiental en tiempo real.
- **Escanear:** Buscar sensores cercanos.
- **Incidencias:** Reportar problemas.
- **Perfil:** Editar tus datos.

### 3.3 Escaneo de Sensores
Esta es la función principal de la App.
1. Dirígete a la sección de **Escáner** o la pantalla principal.
2. La app buscará automáticamente señales **iBeacon** (sensores Atmos).
3. Verás en pantalla valores como:
   - **Temperatura**
   - **Nivel de Ozono (Gas)**
4. Los datos se envían automáticamente al servidor si tienes conexión.

> **Nota:** Es necesario conceder permisos de ubicación y Bluetooth para que el escaneo funcione.

### 3.4 Mapa de Contaminación
Accede a la sección **Mapas** para ver una representación visual de la calidad del aire.
- Los puntos en el mapa indican mediciones recientes.
- **Colores:** Indican la gravedad (ej. Verde = Bueno, Rojo = Malo).
- Puedes filtrar por tipos de contaminantes o fechas.

### 3.5 Gestión de Incidencias
Si detectas un problema (sensor dañado, lectura anómala, zona contaminada visualmente):
1. Ve a **Incidencias**.
2. Pulsa en "Nueva Incidencia".
3. Describe el problema y, si es posible, la ubicación se adjuntará automáticamente.
4. Podrás ver el estado de tus incidencias reportadas en el listado.

### 3.6 Vinculación de Sensores
Para usuarios con permisos o propietarios de sensores:
- Usa la opción **Vincular Sensor** o **Escanear QR**.
- Escanea el código QR del dispositivo físico para asociarlo a tu cuenta o red.

---

## 4. Portal Web

La web ofrece una visión más amplia y administrativa.

### 4.1 Acceso y Dashboard
1. Entra a la URL del proyecto.
2. Inicia sesión con las mismas credenciales que en la App.
3. El **Dashboard (Landing)** muestra un resumen general del proyecto y enlaces rápidos.

### 4.2 Funcionalidades Web
- **Mapas Globales:** Visualiza datos acumulados de todos los usuarios.
- **Tabla de Datos:** Consulta listados históricos de mediciones.
- **Perfil:** Puedes cambiar tu contraseña y editar datos personales (nombre, foto de perfil).
- **Incidencias Web:** También puedes consultar y gestionar las incidencias desde el navegador.

---

## 5. Hardware (Sensores)
El sistema utiliza microcontroladores (como Arduino) configurados para emitir tramas **iBeacon**.
- El dispositivo debe estar encendido para emitir.
- **No requiere emparejamiento manual** en los ajustes de Bluetooth del móvil; la App lo detecta automáticamente.
- Si el sensor tiene LEDs, una luz parpadeante suele indicar que está emitiendo correctamente.

---

## 📄 Exportar a PDF
Para guardar este manual como PDF:
1. Abre este archivo en VS Code u otro editor Markdown.
2. Usa la función de "Imprimir" o una extensión como "Markdown PDF".
3. Selecciona "Guardar como PDF".
