# Fase 3: Análisis de código estático con SonarQube

## Objetivo

El propósito de esta fase fue revisar el código del proyecto con una herramienta de análisis estático para identificar posibles problemas de seguridad, calidad y mantenibilidad.

Para este ejercicio se utilizó **SonarQube Community**, ya que permite revisar el código de forma automática y detectar situaciones que podrían convertirse en errores, vulnerabilidades o dificultades de mantenimiento.

El análisis se realizó sobre el proyecto:

`secure-coding-challenge`

---

## Resultado general

El análisis reportó los siguientes hallazgos:

| Categoría | Cantidad |
|---|---:|
| Seguridad | 1 |
| Confiabilidad | 0 |
| Mantenibilidad | 4 |

En cuanto a severidad, se encontró:

| Severidad | Cantidad |
|---|---:|
| Blocker | 0 |
| High | 1 |
| Medium | 0 |
| Low | 3 |
| Info | 1 |

El hallazgo más importante está relacionado con la configuración de **CSRF en Spring Security**. Los demás corresponden principalmente a recomendaciones de mantenibilidad y buenas prácticas de programación.

---

# Hallazgos encontrados

## 1. Protección CSRF deshabilitada

**Archivo:** `SecurityConfig.java`  
**Categoría:** Seguridad  
**Severidad:** High  

SonarQube reportó el siguiente mensaje:

> Make sure disabling Spring Security's CSRF protection is safe here.

### ¿Qué significa?

CSRF, o **Cross-Site Request Forgery**, es un tipo de ataque en el que un usuario autenticado puede ser inducido a ejecutar una acción que realmente no quería realizar.

Esto es especialmente importante cuando una aplicación utiliza sesiones o cookies para mantener la autenticación, porque el navegador puede enviar esas credenciales automáticamente.

En el proyecto se encontró una configuración donde la protección CSRF está deshabilitada, por ejemplo:

```java
http.csrf(csrf -> csrf.disable());
```

Desactivar CSRF no significa automáticamente que exista una vulnerabilidad, pero sí obliga a revisar si la arquitectura realmente hace segura esa decisión.

### Posible impacto

Si la aplicación utiliza sesiones o cookies y CSRF está deshabilitado, un atacante podría intentar ejecutar operaciones en nombre de un usuario autenticado.

Esto podría generar situaciones como:

- modificación de información;
- eliminación de datos;
- ejecución de acciones no deseadas;
- cambios de configuración;
- operaciones realizadas sin intención del usuario.

En este caso, el principal impacto estaría sobre la **integridad** de la información.

### Mitigación

Si la aplicación utiliza sesiones o cookies, lo recomendable es mantener habilitada la protección CSRF.

Si se trata de una API REST completamente stateless y la autenticación se realiza mediante JWT o Bearer Token en el header `Authorization`, puede ser válido deshabilitar CSRF.

En ese caso, también debería configurarse la aplicación para no mantener sesión:

```java
http
    .csrf(csrf -> csrf.disable())
    .sessionManagement(session ->
        session.sessionCreationPolicy(
            SessionCreationPolicy.STATELESS
        )
    );
```

La recomendación es:

- mantener CSRF habilitado cuando se usen cookies o sesiones;
- usar `SessionCreationPolicy.STATELESS` en APIs REST con JWT;
- verificar que las credenciales no dependan de cookies enviadas automáticamente;
- documentar claramente por qué se deshabilitó CSRF.

---

## 2. Import `RuleType` no utilizado

**Archivo:** `StaticCodeAnalysisReport.java`  
**Categoría:** Mantenibilidad  
**Severidad:** Low  

SonarQube identificó que el siguiente import no se utiliza:

```java
import org.sonar.api.rules.RuleType;
```

### ¿Por qué importa?

Aunque no representa una vulnerabilidad de seguridad, tener imports innecesarios hace que el código sea más difícil de leer y puede generar confusión durante el mantenimiento.

### Mitigación

Eliminar el import:

```java
import org.sonar.api.rules.RuleType;
```

Es una corrección sencilla que ayuda a mantener el código más limpio.

---

## 3. Import `List` no utilizado

**Archivo:** `StaticCodeAnalysisReport.java`  
**Categoría:** Mantenibilidad  
**Severidad:** Low  

SonarQube también encontró que:

```java
import java.util.List;
```

ya no se utiliza en la clase.

Esto probablemente ocurrió después de cambiar una estructura basada en `List<InputFile>` por una basada en `Iterable<InputFile>`.

### Mitigación

Eliminar:

```java
import java.util.List;
```

No afecta la funcionalidad, pero mejora la claridad del archivo.

---

## 4. Condición `if-then-else` innecesariamente compleja

**Archivo:** `InputValidator.java`  
**Categoría:** Mantenibilidad  
**Severidad:** Low  

SonarQube recomienda simplificar una condición que devuelve directamente `true` o `false`.

Por ejemplo:

```java
if (condition) {
    return true;
} else {
    return false;
}
```

puede escribirse de forma más simple:

```java
return condition;
```

### Posible impacto

No se trata de una vulnerabilidad de seguridad, pero simplificar este tipo de lógica:

- mejora la legibilidad;
- reduce líneas innecesarias;
- facilita el mantenimiento;
- disminuye la complejidad del método.

### Mitigación

Reemplazar estructuras redundantes por retornos directos cuando sea posible.

---

## 5. Uso de API antigua para fechas

**Archivo:** `JwtTokenUtil.java`  
**Categoría:** Mantenibilidad  
**Severidad:** Info  

SonarQube recomienda utilizar la API moderna de Java para manejar fechas y tiempos:

```java
java.time
```

en lugar de depender directamente de clases antiguas como:

```java
Date
Calendar
SimpleDateFormat
```

### ¿Por qué es importante?

En una clase relacionada con JWT, el manejo correcto del tiempo es importante porque normalmente se calculan fechas de creación y expiración de tokens.

La API `java.time` es más clara, moderna y segura para este tipo de operaciones.

### Mitigación

En lugar de:

```java
Date expiration = new Date(
    System.currentTimeMillis() + 3600000
);
```

puede utilizarse:

```java
Instant expiration = Instant.now()
        .plus(Duration.ofHours(1));
```

Si la librería JWT requiere un `Date`, se puede hacer la conversión al final:

```java
Date expiration = Date.from(
    Instant.now()
        .plus(Duration.ofHours(1))
);
```

De esta forma se aprovecha `java.time` para los cálculos y solo se convierte al tipo requerido cuando es necesario.

---

# Resumen de hallazgos

| Hallazgo | Archivo | Categoría | Severidad | Recomendación |
|---|---|---|---|---|
| CSRF deshabilitado | `SecurityConfig.java` | Seguridad | High | Validar si la arquitectura es stateless; mantener CSRF si hay sesiones/cookies |
| Import `RuleType` no utilizado | `StaticCodeAnalysisReport.java` | Mantenibilidad | Low | Eliminar import |
| Import `List` no utilizado | `StaticCodeAnalysisReport.java` | Mantenibilidad | Low | Eliminar import |
| `if-then-else` innecesario | `InputValidator.java` | Mantenibilidad | Low | Simplificar a un retorno directo |
| Uso de API antigua de fechas | `JwtTokenUtil.java` | Mantenibilidad | Info | Utilizar `java.time` |

---

# Recomendaciones generales

A partir de los resultados obtenidos, las principales recomendaciones son:

- Revisar con especial atención la configuración de Spring Security.
- Confirmar si la desactivación de CSRF está realmente justificada.
- Mantener el código limpio eliminando imports y estructuras innecesarias.
- Utilizar APIs modernas de Java para el manejo de fechas.
- Ejecutar SonarQube de forma periódica para evitar que nuevos problemas se acumulen.
- Complementar el análisis estático con pruebas unitarias, pruebas de integración y revisión de código.

---

# Conclusión

El análisis con SonarQube permitió identificar cinco hallazgos dentro del proyecto.

De ellos, solo uno está directamente relacionado con seguridad: la desactivación de la protección CSRF en `SecurityConfig.java`.

Este hallazgo requiere revisar cómo funciona la autenticación del proyecto. Si la aplicación usa sesiones o cookies, desactivar CSRF puede representar un riesgo importante. Si, por el contrario, se trata de una API REST stateless que utiliza JWT mediante el header `Authorization`, la desactivación puede estar justificada siempre que la configuración sea coherente con ese modelo.

Los otros cuatro hallazgos están relacionados con mantenibilidad y buenas prácticas. Aunque no representan vulnerabilidades por sí solos, corregirlos ayuda a mantener un código más claro, moderno y fácil de sostener.

En conclusión, SonarQube no reemplaza las pruebas ni la revisión manual, pero sí sirve como una herramienta útil para detectar problemas de manera temprana y mejorar continuamente la calidad y seguridad del proyecto.
