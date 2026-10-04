# Pokemon Stadium Lite

Miniaplicacion Java para consultar Pokemon en PokeAPI y simular combates por turnos.

## Requisitos

- Java 11 o superior.
- Conexion a internet para consultar PokeAPI.

## Compilacion y prueba de logica

Desde la raiz del proyecto:

```bash
rm -rf /tmp/lab-ds3-classes
mkdir -p /tmp/lab-ds3-classes
javac -cp 'lib/*' -d /tmp/lab-ds3-classes src/*.java
java -ea -cp /tmp/lab-ds3-classes BattleLogicTest
```

Las pruebas cubren el modelo `Pokemon`, HP, velocidad, desempate, dano minimo,
golpes criticos, efectividad de tipos, finalizacion y notificacion del ganador.

## Modelo y API

`Pokemon` conserva los datos basicos devueltos por PokeAPI, incluyendo nombre,
tipos, sprite, estadisticas, HP maximo y HP actual. El HP se restaura al iniciar
una batalla y nunca puede quedar por debajo de cero.

`PokeApiClient` consulta solo lectura el endpoint:

```text
https://pokeapi.co/api/v2/pokemon/{name}
```

Acepta nombres normalizados y consultas aleatorias por identificador. Los errores
de red, respuestas HTTP no exitosas, Pokemon inexistentes y JSON incompleto se
entregan como `PokeApiException` para que la interfaz los muestre sin cerrar la
aplicacion.

## Reglas del combate

- Comienza el Pokemon con mayor velocidad.
- Si empatan, se elige aleatoriamente.
- Se usa el primer tipo de cada Pokemon para la efectividad.
- Agua contra Fuego, Fuego contra Planta y Planta contra Agua aplican `x1.3`.
- Las relaciones inversas aplican `x0.7`; los demas casos aplican `x1.0`.
- La probabilidad de golpe critico es 10% y su multiplicador es `x1.5`.
- La formula implementada es:

```text
danoBase = ataqueAtacante * random(0, 1)
          - defensaDefensor * random(0, 1)

dano = redondear(max(0, danoBase * modificadorTipo * modificadorCritico))
```

El dano aplicado se comunica mediante `BattleListener`, junto con los cambios
de HP y el ganador. `Battle` no depende de componentes Swing.

## Ejecucion de consultas

La clase `PokeApi` permite probar el cliente desde consola:

```bash
java -cp '/tmp/lab-ds3-classes:lib/*' PokeApi pikachu
```

La interfaz Swing se inicia desde `PokeApiGUI` usando el formulario asociado
`src/PokeApiGUI.form` en el IDE configurado para el proyecto.
