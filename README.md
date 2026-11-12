# videojuego-tanques-java (ProyectoU3)

[![CI](https://github.com/Demiannnnnn/videojuego-tanques-java/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/Demiannnnnn/videojuego-tanques-java/actions/workflows/ci.yml)

Videojuego de estrategia y combate de tanques por turnos en 2D desarrollado en Java con JavaFX. Los jugadores (humanos y bots) compiten en escenarios con terrenos destructibles calculando ángulo, velocidad, gravedad y viento para impactar a sus contrincantes, gestionando su saldo para adquirir proyectiles en la tienda entre rondas.

## Stack tecnológico

- **Lenguaje:** Java 21
- **Interfaz gráfica y audio:** JavaFX 21 (`javafx-controls`, `javafx-media`)
- **Herramientas de construcción:** Maven (`pom.xml`) y Apache Ant / NetBeans (`build.xml`, `nbproject/`)
- **Testing:** JUnit 5 (JUnit Jupiter 5.12.2)

## Requisitos

- Java Development Kit (JDK) 21 o superior
- Apache Maven 3.9+

## Ejecución de pruebas

Para ejecutar las pruebas unitarias:

```bash
cd Terreno
mvn test
```

Para ejecutar la verificación completa (compilación, pruebas y empaquetado):

```bash
cd Terreno
mvn -B verify
```

## Ejecución del juego

La clase principal de entrada es `terreno.PantallaInicial`.

### Desde NetBeans / IDE:
Abrir el proyecto en `Terreno/` y ejecutarlo utilizando el JDK 21 configurado junto con las librerías JavaFX provistas en `Terreno/lib/`.

### Con Ant:
```bash
cd Terreno
ant run
```
