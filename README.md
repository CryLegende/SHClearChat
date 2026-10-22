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

## Commandes

| Commande | Alias | Description | Permission |
|---|---|---|---|
| `/clearchat` | `/cc` | Commande pour nettoyer le chat | — |

## Permissions

| Permission | Description | Par défaut |
|---|---|---|
| `shclearchat.use` | Permet d'utiliser la commande clearchat | `op` |
| `shclearchat.bypass` | Permet d'éviter le nettoyage du chat global | `op` |
| `shclearchat.other` | Permet de nettoyer le chat d'autres joueurs | `op` |

## Configuration

Fichiers par défaut repris du serveur (secrets remplacés par `CHANGE_ME`). Cliquez sur un fichier pour l'ouvrir :

<pre>
plugins/SHClearChat/
└── <a href="src/main/resources/config.yml">config.yml</a>
</pre>

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
