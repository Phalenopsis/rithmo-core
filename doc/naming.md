# 🧭 Rithmo Core – Naming Conventions (anti-dérive)

---

# 🧱 1. Règle fondamentale : éviter les redondances sémantiques

## ❌ Interdit (anti-pattern)

```java id="n1k9qz"
session.getSessionId()
turn.getTurnState()
game.getGameState()
```

👉 problème : duplication du concept dans le nom

---

## ✅ Correct

```java id="m4v8pl"
session.getId()
turn.getState()
game.getState()
```

👉 règle :

> le contexte porte le sens, pas le nom de la méthode

---

# 🧠 2. Entities naming rule (core domain objects)

## 📦 GameSession

### ✔ responsable de :

* identité du jeu
* joueur courant
* état global

### naming :

```java id="g7p2rt"
GameSession.getId()
GameSession.getState()
GameSession.getCurrentPlayer()
```

❌ pas :

* `getGameId()`
* `getSessionId()`
* `getGameState()`

---

## 🔁 TurnState

### ✔ représente :

* snapshot du tour
* phase + options + state

### naming :

```java id="t9c1wx"
TurnState.getState()
TurnState.getPhase()
TurnState.getAvailableMoves()
```

❌ pas :

* `getTurnState()` (redondant)
* `getCurrentTurnState()`

---

## 🎮 GameState (engine)

### ✔ pure data engine

```java id="e2k6rq"
GameState.board()
GameState.currentPlayer()
```

👉 pas de prefix “get” obligatoire ici (engine = minimal API clean)

---

# 🧭 3. Service naming rule

## ✔ GameService (facade)

### doit être VERBE-driven

```java id="s4n8yt"
startGame()
playTurn()
getTurnState()
```

❌ éviter :

* `processGame()`
* `handleGame()`
* `updateGameState()`

👉 règle :

> services = actions, pas getters

---

# ⚙️ 4. TurnProcessor naming rule

## ✔ doit être orchestration verb-driven

```java id="p6m2kd"
process(state, action)
```

❌ pas :

* `applyTurn()`
* `updateTurnState()`
* `executeTurn()`

👉 règle :

> un seul verbe universel = `process`

---

# 🎯 5. Action naming rule

## ✔ must express intent

```java id="a3v9lf"
PreCaptureAction
MoveAction
PostCaptureAction
```

❌ éviter :

* `CaptureMove`
* `MoveCaptureAction`
* `TurnMove`

👉 règle :

> Action = intention utilisateur, pas résultat engine

---

# 🧩 6. Resolver naming rule

## ✔ always “resolveXxx”

```java id="r8p1tc"
resolvePreCaptures()
resolvePostCaptures()
```

❌ pas :

* `getCaptures()`
* `computeCapturesOptions()`

👉 règle :

> resolver = transformation Engine → Core model

---

# 🔧 7. Applier naming rule

## ✔ always “applyXxx”

```java id="ap7x1q"
applyMove()
applyCaptures()
```

❌ pas :

* `executeMove()`
* `doMove()`
* `updateBoard()`

👉 règle :

> applier = mutation explicite (sans ambiguïté)

---

# 🧠 8. Engine naming rule (external)

## ✔ verbs based on computation

```java id="e9k4qz"
generateMoves()
findCaptures()
checkVictory()
```

👉 règle :

> engine = “compute / find / generate”

---

# 🚫 9. Forbidden naming patterns (important)

## ❌ Anti-patterns à éviter absolument

### 1. Redundant prefixes

```java
getGameGameState()
getTurnTurnState()
```

---

### 2. Mixed responsibility names

```java
processAndApplyMove()
computeAndApplyCapture()
```

---

### 3. UI leakage in core

```java
selectedCaptures
isSelected
```

---

# 🧭 10. Mental model (ultra important)

```text id="z9k3wp"
Entity      → nouns (Game, Turn, Session)
Service     → verbs (start, play, get)
Processor   → process()
Resolver    → resolveX()
Applier     → applyX()
Engine      → compute / generate / find
```

---

# 🔥 11. Golden Rule finale

> If you hesitate between two names, choose the one that removes redundancy with context.

---

# 💡 Exemple concret (ton cas)

## ❌ mauvais

```java id="x2k9pl"
session.getSessionId()
turn.getTurnState()
```

## ✅ correct

```java id="k7m1rq"
session.getId()
turn.getState()
```

---

# 🚀 Résultat attendu

Avec ces règles :

✔ moins de confusion mentale
✔ moins de duplication conceptuelle
✔ core plus lisible
✔ architecture plus stable dans le temps

