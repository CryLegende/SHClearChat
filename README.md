<div align="center">

<img src="assets/logo.png" alt="SkyHand" width="140">

# SHClearChat

*Nettoie le chat de tout le serveur, le sien ou celui d'un autre joueur (/clearchat).*

<img src="https://img.shields.io/badge/Paper-26.3-f2a900?style=for-the-badge" alt="Paper 26.3"> <img src="https://img.shields.io/badge/Java-25-e76f00?style=for-the-badge" alt="Java 25"> <img src="https://img.shields.io/badge/SkyHand-Opale%20%C2%B7%20Lobby-3aa655?style=for-the-badge" alt="SkyHand Opale · Lobby">

</div>

---

## Présentation

Nettoie le chat de tout le serveur, le sien ou celui d'un autre joueur (/clearchat).

Utilisé sur : **Opale · Lobby**.

Avec **SHNetwork** (facultatif), le nettoyage général atteint les joueurs de tous les serveurs du réseau et `/clearchat <joueur>` trouve un joueur connecté sur un autre serveur. Sans SHNetwork, ou avec `reseau.actif: false`, seuls les joueurs du serveur sont atteints.

## Commandes

| Commande | Alias | Description | Permission |
|---|---|---|---|
| `/clearchat` | `/cc` | Nettoie le chat de tous les joueurs (sauf `shclearchat.bypass`) | `shclearchat.use` |
| `/clearchat self` | `/cc self` | Nettoie son propre chat | — (tous les joueurs) |
| `/clearchat <joueur>` | `/cc <joueur>` | Nettoie le chat d'un joueur | `shclearchat.other` |
| `/shclearchat reload` | — | Relit `config.yml` et `messages.yml` ; si l'un est invalide, rien n'est remplacé et le message donne le fichier et l'erreur | `shclearchat.admin.reload` |

## Permissions

| Permission | Description | Par défaut |
|---|---|---|
| `shclearchat.use` | Permet de nettoyer le chat de tout le monde avec /clearchat | `op` |
| `shclearchat.bypass` | Permet d'éviter le nettoyage du chat global | `op` |
| `shclearchat.other` | Permet de nettoyer le chat d'un autre joueur avec /clearchat <joueur> | `op` |
| `shclearchat.admin` | Toutes les sous-commandes de /shclearchat | `op` |
| `shclearchat.admin.reload` | Utiliser /shclearchat reload | `op` |

## Configuration

Fichiers par défaut, avec des valeurs d'exemple. Cliquez sur un fichier pour l'ouvrir :

<pre>
plugins/SHClearChat/
├── <a href="src/main/resources/config.yml">config.yml</a>
└── <a href="src/main/resources/messages.yml">messages.yml</a>
</pre>

Au premier démarrage avec `messages.yml` absent, il est créé à partir de l'ancienne section `messages:` de `config.yml` (textes déjà personnalisés conservés) ; cette section n'est plus lue ensuite.

La clé `reseau.actif` (par défaut `true`) active l'envoi vers les autres serveurs quand SHNetwork est présent. Elle se lit au démarrage.

## Compilation

Java 25 et Maven :

```bash
mvn clean package
```

Le jar est généré dans `target/`.

---

<div align="center">

<sub>Développé par <b>Cry_Legende</b> pour le serveur <b>SkyHand</b> · Tous droits réservés</sub>

</div>
