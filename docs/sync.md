# Synchronisation (offline-first)

Ce document décrit la synchronisation de l'application et le rôle des ViewModels, repositories de sync et du `UnifiedSyncManager` (source unique pour déclenchement + état global de sync).

## Objectif

- Persister localement d'abord (Room).
- Déclencher une synchronisation vers Supabase après chaque mutation utilisateur.
- Garder un ordre de sync stable pour respecter les dépendances entre entités.

## Vue d'ensemble

1. L'utilisateur modifie des données via un écran.
2. Le ViewModel exécute le use case (écriture locale).
3. En cas de succès, le ViewModel appelle `requestSync(reason, debounceMs)`.
4. `UnifiedSyncManager` applique le debounce puis déclenche une sync globale.
5. Les repositories de sync envoient/reçoivent les données dans l'ordre défini.

## Déclencheurs ViewModel

La sync est demandée après mutation réussie sur :

- Housing: create, update, delete
- Tenant: create, update, delete
- Lease: create, close
- Key: add, delete
- Indexation: apply
- Auth: login/restore (déclenchement immédiat via `debounceMs = 0`)

## Contrat de debounce

Le contrat `HousingSyncRequester` expose `requestSync(reason: String, debounceMs: Long = 800)`.

### Comportement actuel

- `UnifiedSyncManager` respecte désormais le paramètre `debounceMs`.
- Si plusieurs demandes arrivent rapidement, la dernière remplace les précédentes.
- `debounceMs < 0` est ramené à `0`.
- `debounceMs = 0` provoque une demande immédiate (utile au login/restore).

Ce comportement évite de lancer trop de sync successives tout en laissant la main aux appels urgents.

## Ordre de synchronisation

Ordre appliqué par `UnifiedSyncManager` :

1. Tenants
2. Housings
3. Leases
4. Keys
5. IndexationEvents

Cet ordre respecte les dépendances de clés étrangères (ex. lease dépend de tenant/housing).

## État de synchronisation exposé à l'UI

Le contrat `HousingSyncStateObserver` expose :

- `state: StateFlow<SyncState>` avec `Idle`, `Syncing`, `Error(message)`
- `consumeError()` pour remettre l'état à `Idle` après affichage UI

`AuthGate` observe cet état pour :

- afficher une barre de progression pendant `Syncing`
- afficher un message en cas de `Error`

## Gestion d'erreur

- Les erreurs sont collectées par étape (tenant, housing, lease, key, indexation).
- La sync continue sur les étapes suivantes autant que possible.
- En fin de cycle :
  - pas d'erreur => `SyncState.Idle`
  - une ou plusieurs erreurs => `SyncState.Error(message consolidé)`
- Une demande ultérieure peut rattraper l'état.

## Notes d'architecture

- Les ViewModels ne connaissent pas les détails réseau : ils demandent une sync via `HousingSyncRequester`.
- L'UI globale ne dépend pas d'une implémentation concrète : elle observe l'état via `HousingSyncStateObserver`.
- `UnifiedSyncManager` implémente ces deux interfaces et centralise la stratégie (debounce, ordonnancement, robustesse, état UI).
- Les repositories de sync restent responsables de la sérialisation et des mappings.
