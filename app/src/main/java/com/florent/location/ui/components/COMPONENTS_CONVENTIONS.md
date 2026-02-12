# Conventions de rangement `ui/components`

Pour éviter l'accumulation dans un fichier unique, ranger les composants par responsabilité :

- `Cards.kt` : composants carte/surfaces de présentation.
- `States.kt` : composants d'état (loading/empty/error) et éléments de texte/labels.
- `Fields.kt` : champs de saisie, formats d'affichage et helpers privés associés.
- `Actions.kt` : scaffolds et composants d'actions utilisateur (boutons, rangées d'actions).

## Règles d'évolution

1. **Ne pas recréer de fichier "fourre-tout"** (ex: `UiElements.kt`).
2. **Conserver les API publiques stables** (mêmes noms/signatures) pour éviter de casser les écrans.
3. **Garder les helpers en `private`** dans le module qui les utilise.
4. **Limiter les imports** à ceux réellement utilisés (supprimer doublons et imports morts).
5. Si un nouveau groupe de composants grossit, créer un nouveau fichier thématique dédié.
