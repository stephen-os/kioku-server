# API Surface

The complete server interface. Desktop, website and mobile all use this same
surface; there is no per-client API.

Companion to [sync-contract.md](sync-contract.md), which defines how conflicts
resolve and what the sync columns mean. This document defines the endpoints.

## The model

Clients hold a full local replica. They read and write locally, then reconcile
with the server. A user action like "add a tag to this card" is **not a network
call**: it is a local edit to `card.tag_ids` that ships on the next push.

This replaces the original API, which was fine-grained RPC:

```
POST   /cards/{cardId}/tags/{tagId}     one tag attached, one round trip
DELETE /cards/{cardId}/tags/{tagId}     one tag detached, one round trip
PUT    /decks/{deckId}                  one deck updated
```

That design assumes the client owns nothing and asks the server to mutate state
on its behalf. It cannot work offline, needs a round trip per edit, and gives
each client a chance to implement the rules differently. All three problems
disappear when content moves in bulk.

## Endpoints

### Auth

| Method | Path | Status |
| --- | --- | --- |
| POST | `/auth/register` | exists |
| POST | `/auth/login` | exists |
| POST | `/auth/refresh` | **to build** |
| POST | `/auth/password-reset/request` | **to build** (columns exist) |
| POST | `/auth/password-reset/confirm` | **to build** (columns exist) |

### Account

| Method | Path | Status |
| --- | --- | --- |
| GET | `/account` | exists |
| PATCH | `/account/email` | exists |
| PATCH | `/account/password` | exists |
| DELETE | `/account` | exists |

Account data is server-authoritative and never synced. There is no offline edit
path for it.

### Sync

Two endpoints carry all content, for every client.

| Method | Path | Status |
| --- | --- | --- |
| GET | `/sync?since={seq}` | **to build** |
| POST | `/sync` | **to build** |

#### Pull

```
GET /sync?since=4783
```

Returns every entity owned by the caller whose `server_seq` is greater than
`since`, tombstones included, plus the new high-water mark. Omitting `since`
means a full sync.

```json
{
  "seq": 4812,
  "decks":     [ { "id": "…", "name": "…", "updatedAt": "…", "deletedAt": null, "serverSeq": 4790 } ],
  "tags":      [ … ],
  "cards":     [ … ]
}
```

A `since` older than the tombstone horizon (90 days) is answered with
`409 Conflict` and a full-resync instruction, because deletions the client
never saw may already have been purged.

#### Push

```
POST /sync
```

```json
{
  "decks": [ … ],
  "tags":  [ … ],
  "cards": [ … ]
}
```

The server applies the conflict rule per entity, assigns `server_seq`, and
responds with the accepted high-water mark plus any entity where the stored
copy won, so the client can reconcile:

```json
{
  "seq": 4820,
  "rejected": {
    "cards": [ { "id": "…", "…": "server copy" } ]
  }
}
```

Push is idempotent. Re-sending an entity whose `updated_at` matches the stored
copy is a no-op, which is what makes retry-after-timeout safe.

A client `updated_at` more than 5 minutes in the future is rejected with a
clock-skew error rather than silently winning every conflict.

## Removed

These were part of the RPC design and have no place in the new one.

| Removed | Why |
| --- | --- |
| `DeckController` | Deck changes travel in `/sync` |
| `CardController` | Card changes travel in `/sync`, including tag membership |
| `TagController` | Tag changes travel in `/sync` |
| `ImportController` | Clients hold the full dataset, so parsing a deck file is a local operation that produces entities and pushes them |
| `ExportController` | Same in reverse: the client already has everything it needs to serialize |
| Response DTOs for the above | Replaced by the sync payload shapes |
| Entity graph helpers (`addCard`, `removeTag`, `getTagsInUse`, …) | These were server-side mutations driven by RPC calls. Clients now do this locally |

Import and export becoming client-side is a consequence of replication, not a
feature cut. The server never needs to know the file format.

## Versioning

The API is versioned from the first release. A client can be offline across a
schema change, so sync payloads must tolerate unknown fields rather than
failing on them.
