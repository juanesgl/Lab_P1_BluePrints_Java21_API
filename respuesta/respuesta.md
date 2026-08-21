# Mini-Doc: Desarrollo del Laboratorio

--- 

## 1) Familiarización con el código base

- Para esta parte primeramente revisamos el paquete de `model`, encontramos dos clases, `Blueprint` y `Point`.
  - En `Blueprint` nos damos cuenta de que es clase es un módelo que representa un plano digital compuesto por 
  autor, nombre identificador, y una secuencia de puntos o coordenadas. 
  - Además de esto tiene las siguientes características: 
    - Identificación del plano: Almacena el creador (`author`) y el título del plano (`name`).
    - Manejo de coordenadas: Mantiene una lista interna de objetos `Point (points)`. Permite inicializarla desde el constructor o agregar puntos individuales mediante `addPoint(Point p)`.
    - Encapsulamiento e inmutabilidad parcial: El método `getPoints()` retorna `Collections.unmodifiableList(points)`, lo que impide que agentes externos modifiquen la lista directamente sin usar los métodos de la clase.
    - Identidad e igualdad (`equals y hashCode`): Dos instancias de Blueprint se consideran equivalentes si coinciden exactamente en su autor y nombre, independientemente de los puntos que contengan. Esto permite usar el par (`author, name`) como clave única en estructuras como `HashSet` o `HashMap`.

  - En `Point` se encuentra es un `record`, representando coordenadas bidimensionales `(x, y)`.
    - Contiene lo siguiente: 
      - Campos inmutables: Dos atributos privados y finales `(private final int x, private final int y)`.
      - Constructor canónico: `public Point(int x, int y)` para inicializar ambos valores.
      - Getters de lectura: Métodos de acceso `x()` e `y()`.
      - Comparación por valor `(equals y hashCode)`: Dos puntos se consideran idénticos si sus valores x e y coinciden, permitiendo comparaciones directas y su uso seguro en colecciones.
      - Representación textual (toString): Devuelve una cadena legible con el formato `Point[x=..., y=...]`.

- Seguido de `model`, revisamos la carpeta de `persistence` y encontramos dos clases de excepciones, una interfaz y una clase.

    - En `BlueprintNotFoundException` y `BlueprintPersistenceException`:

        - Son clases de excepción personalizadas que extienden de `Exception` (excepciones verificadas o *checked exceptions*).

        - `BlueprintNotFoundException`: Se dispara cuando se solicita un plano o un autor que no existe en el repositorio.

        - `BlueprintPersistenceException`: Se dispara cuando ocurre un error al registrar o procesar un plano (por ejemplo, intentar guardar un plano que ya existe).

    - En `BlueprintPersistence`:

        - Es la interfaz que define el contrato para el almacenamiento y consulta de planos, permitiendo desacoplar la lógica de negocio del mecanismo de almacenamiento concreto.

        - Define operaciones para: guardar un plano (`saveBlueprint`), consultar un plano por autor y nombre (`getBlueprint`), consultar todos los planos de un autor (`getBlueprintsByAuthor`), obtener todos los planos registrados (`getAllBlueprints`) y agregar puntos a un plano existente (`addPoint`).

    - En `InMemoryBlueprintPersistence`:

        - Es la implementación en memoria de `BlueprintPersistence`, anotada con `@Repository` para ser gestionada como un bean por el contenedor de inversión de control de Spring.

        - **Almacenamiento concurrente:** Utiliza un `ConcurrentHashMap<String, Blueprint>` donde la llave se forma concatenando `author:name`, garantizando operaciones seguras en entornos multihilo.

        - **Carga inicial:** En su constructor precarga datos de prueba con tres planos predefinidos (`bp1`, `bp2`, `bp3`).

        - **Lógica de persistencia y consulta:** Valida la existencia de elementos antes de insertar o retornar datos, lanzando las excepciones `BlueprintPersistenceException` o `BlueprintNotFoundException` según corresponda, y utiliza flujos (`Streams`) para filtrar planos por autor.

- Continuamos con el paquete de `services`, donde encontramos la clase de servicio principal del sistema.

    - En `BlueprintsServices`:

        - Es la clase que encapsula la lógica de negocio de la aplicación, anotada con `@Service` para que Spring la gestione como un bean de servicio e inyecte automáticamente sus dependencias mediante constructor.

        - **Inyección de dependencias:** Recibe una implementación de la interfaz `BlueprintPersistence` (para el acceso a datos) y una de `BlueprintsFilter` (para aplicar transformaciones o filtros sobre los puntos del plano).

        - **Operaciones principales:**

            - **Registro de planos:** El método `addNewBlueprint(Blueprint bp)` delega el guardado a la capa de persistencia, propagando `BlueprintPersistenceException` si ya existe.

            - **Consultas:** Permite obtener todos los planos (`getAllBlueprints`) o los de un autor en específico (`getBlueprintsByAuthor`), propagando `BlueprintNotFoundException` si el autor no tiene registros asociados.

            - **Consulta con filtrado:** El método `getBlueprint(String author, String name)` recupera el plano desde la persistencia y le aplica la estrategia de procesamiento definida en `filter.apply(...)` antes de retornarlo.

            - **Modificación de coordenadas:** Permite añadir nuevos puntos a un plano existente (`addPoint`) delegando la operación directamente a la persistencia.
  
---

