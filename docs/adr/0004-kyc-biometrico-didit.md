# ADR 0004 — KYC biométrico con Didit (proveedor externo)

**Fecha**: 2026-05-15
**Estado**: Aceptada (POC en QA, monitorear antes de prod)
**Contexto previo**: ADR 0001 (secrets), ADR 0002 (JWT), ADR 0003 (Build ID).

## Contexto

El módulo KYC actual de Fatrans valida documentos (cédula, selfie con cédula,
comprobantes) pero no incluye:

- **Liveness detection** (anti-spoofing). Un atacante puede subir una foto impresa
  de la cédula y un selfie estático ajeno y completar el KYC.
- **Face match** entre el selfie y la foto de la cédula.
- **OCR del documento** (los datos los digita el socio manualmente, sin verificación).

Sin liveness, el KYC es trivialmente burlable. Para fintech regulada, **iBeta L2**
(ISO/IEC 30107-3) es lo defensible legalmente. Construir liveness propio expone a
fraude de presentación masivo y LOPDP exige medidas técnicas adecuadas.

## Restricciones del contexto

- **Venezuela**: pagos USD complicados; muchos SaaS KYC (Onfido, Jumio, Veriff,
  Sumsub) rechazan el onboarding del cliente en compliance interno.
- **VPS sin GPU**: descarta self-host de modelos faciales serios.
- **Volumen inicial bajo** (~100–1000 verificaciones/mes en arranque), creciendo
  a ~10k/mes en año 2.
- **LOPDP** clasifica datos biométricos como sensibles → consentimiento explícito,
  separado, versionado y revocable.

## Decisión

Adoptamos **Didit** (didit.me, sede España) como proveedor primario de KYC biométrico:

- **Passive liveness iBeta L1 + face match + ID document OCR + AML/PEP**, todo en
  un solo proveedor.
- **500 verificaciones gratis/mes** (cubre el arranque sin costo).
- Pago **prepago en USD** desde tarjeta venezolana internacional o stablecoin.
- Sin restricción explícita de país para Venezuela.
- API REST + webhooks + widget JS embebible.

El acceso al proveedor se hace mediante un **puerto unificado**
`BiometricVerificatorPort` (hexagonal). Si más adelante Didit deja de servirnos,
cambiamos el adapter sin tocar use cases ni dominio.

## Alternativa de respaldo (Plan B)

**AWS Rekognition Face Liveness + Textract** ($0.05–0.15 por verificación). Solo
si Didit rechaza el onboarding del fondo o si necesitamos iBeta L3 (máscaras 3D y
deepfakes — relevante a mediano plazo). El puerto está diseñado para que el
adapter sea reemplazable.

## Lo que NO elegimos y por qué

| Opción | Por qué no |
|---|---|
| **InsightFace / CompreFace / MediaPipe** (self-host) | Sin liveness ISO/iBeta certificable → riesgo de fraude + incumplimiento LOPDP. |
| **Onfido / Jumio / Veriff / Sumsub** | Compliance interno probable rechazo Venezuela; mínimos mensuales caros. |
| **Truora** (LatAm) | Venezuela no listado oficialmente; pricing no público. |

## Consecuencias

### Positivas

- Liveness iBeta L1 + face match desde día 0.
- $0 costos los primeros 12–18 meses al volumen actual.
- Adapter reemplazable (puerto hexagonal).
- Cumplimiento LOPDP: consentimiento separado, política versionada, revocación
  con borrado verificable en el proveedor.

### Negativas / Riesgos

- **Riesgo geográfico**: si Didit cambia política y restringe Venezuela, hay que
  migrar. Mitigación: puerto abstracto + adapter AWS listo como fallback.
- **iBeta L1 ≠ L3**: pasivos avanzados (deepfakes) pueden colar. Mitigación: el
  analista humano sigue siendo el aprobador final (no aprobación automática).
- **Transferencia internacional de datos biométricos a España**: la LOPDP
  venezolana exige consentimiento expreso que mencione país y proveedor — el
  texto del consentimiento lo hace.
- **Dependencia de webhook**: si Didit no entrega el webhook, el flujo se queda
  colgado. Mitigación: timeout configurable + endpoint manual de consulta a Didit
  (no implementado en el MVP, planificado para fase 2).

## Implementación (PR feature/kyc-biometric-didit)

- Migración Flyway `V13__create_biometric_verification.sql`: tablas
  `biometric_verification` y `biometric_consent` + columna `estado_biometria` en
  `verificacion_kyc`.
- Puerto `BiometricVerificatorPort` con 3 métodos + 4 records DTO.
- Adapter `DiditKycAdapter` con HMAC-SHA256 + ventana de timestamp anti-replay.
- Use cases: `RegistrarConsentimientoBiometrico`, `IniciarVerificacionBiometrica`,
  `ProcesarWebhookBiometrico`, `RevocarConsentimientoBiometrico`.
- Controller `BiometricController` con 4 endpoints (consentimiento, iniciar,
  webhook público, revocar).
- Integración en `RevisarDocumentosUseCase.aprobar()`: bloquea aprobación si
  `estadoBiometria != APROBADA`.
- Componente frontend `BiometricCapture.tsx` que orquesta consentimiento + abrir
  widget Didit en pestaña nueva.

## Configuración necesaria post-merge

Env vars en QA (después en prod tras validar):

```
DIDIT_ENABLED=true
DIDIT_API_KEY=...
DIDIT_WEBHOOK_SECRET=...
DIDIT_WORKFLOW_ID=...
DIDIT_CALLBACK_URL=https://qa-app.fatrans.com.ve/dashboard/kyc
```

Webhook público a exponer en nginx:
`POST https://qa-api.fatrans.com.ve/api/v1/kyc/biometric/webhook` (sin auth JWT,
validación HMAC en el adapter).

## Trabajo diferido

- Integración del consentimiento biométrico en el form de registro (paso 6 del
  stepper) — actualmente solo se pide al entrar al flujo KYC desde el dashboard.
- Endpoint de polling para casos donde el webhook no llega.
- Migración a iBeta L2 cuando volumen lo justifique (Didit Premium o AWS).
- Notificación al socio cuando el resultado biométrico llegue (correo + push).

## Revisión

Reevaluar a los 6 meses (octubre 2026) — métricas clave:
- Tasa de aprobación / tasa de rechazo / tasa de spoof detectado.
- Volumen mensual contra cuota gratis.
- Tiempo desde inicio hasta resultado.
- Incidentes de seguridad / quejas de socios.
