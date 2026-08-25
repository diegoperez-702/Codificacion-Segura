# Implementación de prácticas de codificación segura en un sistema de integración SOA

Un sistema de integración basado en arquitectura orientada a servicios (SOA) necesita fortalecer sus prácticas de codificación segura para mitigar vulnerabilidades comunes y asegurar la integridad de los datos. El sistema integra varios servicios que interactúan entre sí para procesar transacciones financieras. Es crucial implementar validaciones de entrada y utilizar herramientas de análisis de código estático para detectar problemas de seguridad.

## Informacion General

| Campo | Valor |
|-------|-------|
| **Tema** | Codificación segura básica |
| **Nivel** | senior-l1 |
| **Tipo** | mixed |
| **Tiempo estimado** | 4-5 horas |

## Fases del Reto

### Fase 0: Configuración del Proyecto

**Objetivo:** Obtener el proyecto base funcional enviando el Código Base a un asistente de IA, que lo analizará, corregirá errores y generará un ZIP listo para usar.

**Tiempo estimado:** 15-30 minutos

**Instrucciones:**

- Asegúrate de tener instalado para ejecutar el proyecto: Un IDE o editor de código.
- Copia todo el contenido del campo **Código Base** de este reto — incluyendo el texto de instrucciones que aparece al inicio.
- Abre un asistente de IA (Claude en claude.ai, ChatGPT o Gemini — se recomienda Claude), pega el contenido copiado en el chat y envíalo.
- El asistente analizará los archivos, corregirá errores y generará un archivo ZIP descargable. Descárgalo y extráelo en la carpeta donde quieras trabajar.
- Verifica que el proyecto arranca sin errores.

**Entregable:** El proyecto compila/arranca sin errores.

<details>
<summary>Pistas de conocimiento</summary>

- Copia el Código Base completo incluyendo el texto de instrucciones al inicio — esas instrucciones le indican al asistente exactamente qué hacer con los archivos.
- Si el asistente no genera el ZIP automáticamente al terminar el análisis, escríbele: "genera el ZIP ahora".
- Si el proyecto tiene errores al arrancar, comparte el mensaje de error con el mismo asistente para que lo corrija.

</details>

### Fase 1: Identificación de vulnerabilidades comunes

**Objetivo:** Comprender y documentar vulnerabilidades comunes en sistemas de integración SOA.

**Tiempo estimado:** 1 hora

**Instrucciones:**

- Investiga y documenta al menos tres vulnerabilidades comunes en sistemas de integración SOA.
- Describe cómo estas vulnerabilidades pueden afectar la integridad y seguridad del sistema.

**Entregable:** Documento que describe tres vulnerabilidades comunes y su impacto en el sistema.

<details>
<summary>Pistas de conocimiento</summary>

- Considera vulnerabilidades como inyección de SQL, cross-site scripting (XSS), y ejecución remota de código.
- Reflexiona sobre cómo estas vulnerabilidades pueden ser explotadas en un sistema de integración.

</details>

### Fase 2: Implementación de validaciones de entrada

**Objetivo:** Implementar validaciones de entrada para mitigar vulnerabilidades identificadas.

**Tiempo estimado:** 2 horas

**Instrucciones:**

- Identifica puntos de entrada en el sistema donde se reciben datos externos.
- Implementa validaciones de entrada para asegurar que los datos recibidos son seguros y cumplen con los estándares del sistema.

**Entregable:** Código que implementa validaciones de entrada en los puntos identificados.

<details>
<summary>Pistas de conocimiento</summary>

- Utiliza técnicas de validación como comprobación de tipos, longitud, y formato.
- Considera la implementación de listas blancas para entradas conocidas seguras.

</details>

### Fase 3: Uso de herramientas de análisis de código estático

**Objetivo:** Utilizar herramientas de análisis de código estático para detectar problemas de seguridad.

**Tiempo estimado:** 1 hora

**Instrucciones:**

- Selecciona una herramienta de análisis de código estático adecuada para el sistema.
- Ejecuta el análisis y documenta los hallazgos, incluyendo posibles vulnerabilidades y recomendaciones para su mitigación.

**Entregable:** Informe que documenta los hallazgos del análisis de código estático y recomendaciones para mitigación.

<details>
<summary>Pistas de conocimiento</summary>

- Investiga herramientas como SonarQube, Checkmarx, o similar.
- Considera la integración de la herramienta en el proceso de desarrollo para análisis continuo.

</details>

## Dimensiones Evaluadas

- **queEs**: ¿Qué es una vulnerabilidad de inyección de SQL y cómo puede afectar un sistema de integración SOA?
- **paraQueSirve**: ¿Para qué sirven las validaciones de entrada en un sistema de integración?
- **comoSeUsa**: ¿Cómo se usa una herramienta de análisis de código estático para detectar problemas de seguridad?
- **erroresComunes**: ¿Cuáles son errores comunes al implementar validaciones de entrada?

## Criterios de Evaluacion

- Documentar al menos tres vulnerabilidades comunes en sistemas de integración SOA y su impacto.
- Implementar validaciones de entrada en puntos críticos del sistema.
- Utilizar una herramienta de análisis de código estático y documentar hallazgos y recomendaciones.

---

*Reto generado automaticamente por Challenge Generator - Pragma*
