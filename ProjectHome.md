# Qualité de service #

## Motivations ##
À l'origine conçu pour la transmission de pages avec liens hypertextes, le protocole HTTP représente une part croissante du trafic sur Internet. On l'utilise donc désormais pour implanter des services web divers, consulter des messageries (webmail), télécharger des fichiers, transmettre des données audiovisuelles au détriment de protocoles plus spécialisés (IMAP, FTP, RTP...). Nous souhaitons pouvoir prioritiser le transfert des données liées à certaines requêtes HTTP en fonction des différents usages : la discrimination sera réalisée en examinant l'URL de la ressource demandée. On pourra ainsi par exemple pouvoir télécharger la dernière image ISO de sa distribution Linux favorite avec une priorité faible tout en garantissant la visualisation d'une vidéo sans ralentissement.

## Implantation ##
Garantir la qualité de service en émission (envoi de requête HTTP) peut être réalisé en ajustant le débit d'émission de données pour chaque type de ressource. Afin d'implanter des fonctionnalités de qualité de service en réception, nous pouvons créer un en-tête spécifique (non prévu dans le protocole HTTP 1.1) pour demander au serveur distant d'ajuster son débit en émission. Il est cependant nécessaire que le serveur supporte l'usage de ce nouvel en-tête. Si le débit de réception demeure trop important, on pourra le limiter indirectement en ralentissant le rythme de récupération de données envoyées par le serveur distant au proxy. Cela aura pour effet de saturer le buffer en réception de la pile TCP/IP du système et conduire au rejet de segments et ainsi à la réduction de la fenêtre d'envoi du serveur distant. Le débit est ainsi réduit.

## Configuration ##
On devra pouvoir appliquer des règles de QoS pour des catégories de ressources. Une catégorie de ressource est définie par la validation d'un ensemble d'expressions régulières sur l'URL demandée ainsi que sur des valeurs de champs d'en-tête. On pourra configurer pour chaque catégorie différents paramètres (et ce aussi bien pour l'émission que la réception) :
  * Le débit minimal garanti : on devra garantir en toute circonstance le débit spécifié (dans les limites bien sûr de la connexion utilisée).
  * Le débit maximum : on ne devra pas dépasser ce débit. Si le débit maximum est nul, cela équivaut à filtrer la ressource demandée : on retournera au client demandant cette ressource une erreur HTTP.
  * Un poids de priorité : ce poids permet de distribuer la capacité de bande passante en surplus aux différentes catégories de ressource.
On pourra appliquer une règle de QoS aussi bien pour une catégorie de ressources dans sa globalité (par exemple limiter à 50 Ko/s en réception l'ensemble de toutes les requêtes sur des URLs validant l'expression régulière ^.`*`\.(tar\.gz|tar\.bz2|zip)$) ou pour une requête individuelle d'une catégorie. L'ensemble des règles de QoS résident dans un fichier dont on définira et documentera le format. Les expressions régulières d'URLs ou de valeurs d'en-tête respectent la syntaxe utilisée par _java.util.regex.Pattern_. Il faudra bien préciser le comportement à adopter lorsqu'une ressource valide plusieurs règles.

# Cache #

## Cache local ##
Le proxy web maintient dans un cache local (sur le disque) les ressources qui ont été les plus récemment récupérées. Ainsi lorsqu'une ressource est demandée par un client, le cache local est consulté préalablement à toute récupération distante de la ressource :
  * si celle-ci est présente ET que la version semble n'être pas périmée, elle est retournée au client;
  * si un doute existe quant à la fraîcheur de la version, on pourra questionner le serveur distant pour savoir si une version plus récente existe et la télécharger le cas échéant;
  * si la ressource n'est pas présente en cache, on la télécharge sur le serveur distant

On définit pour chaque catégorie d'URLs (validant une expression régulière) une taille maximum d'espace disque alloué pour le cache local (dans un fichier de configuration). On essaiera de compresser les ressources stockées. On proposera une politique judicieuse afin de gérer la suppression de ressources lorsque la totalité de l'espace est utilisée.

On sera attentif à respecter pour le comportement du cache les préconisations énoncées dans la [section 13 de la RFC 2616](http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html#sec13) relative au protocole HTTP/1.1. On devra pouvoir configurer le cache comme partagé ou non-partagé (un cache partagé ne peut pas conserver des données envoyées avec un en-tête Cache-Control: private).

## Partage de cache sur réseau local ##
Si une ressource n'est pas disponible dans le cache local, nous souhaitons faire appel aux caches locaux d'autres proxys présents sur le réseau local. Nous envoyons en UDP multicast une demande de ressource au groupe de proxys sur le réseau : les proxys disposant de cette ressource le signalent au proxy demandeur qui peut alors récupérer cette ressource. Ponctuellement ou alors lors de sa phase d'extinction, un proxy peut annoncer spontanément ses ressources les plus populaires sur le groupe de multicast : des proxys pourront alors s'ils le souhaitent récupérer certaines de ces ressources. Dans tous les cas, un cache ne peut envoyer des données déclarées privées à un autre cache.

Quelques précautions sont à prendre pour assurer la confidentialité des requêtes et données en cache :
  * Lorsqu'un cache demande une ressource, il pourra communiquer une version hachée de son URI en multicast pour éviter de dévoiler publiquement l'historique de navigation de son utilisateur.
  * Le cache ne peut envoyer des données déclarées privées à un autre cache.

# Interface web #
On proposera une interface web permettant de consulter les paramètres de configuration et visualiser l'état de la QoS et du cache. On pourra ainsi consulter sur un intervalle de temps défini le volume de données échangées pour chacune des catégories de QoS. Il sera possible également de consulter l'état d'occupation du cache pour chaque catégorie de ressources ainsi que le vider.

# Consignes #

## RFCs ##
Il vous est demandé dans un premier temps de rédiger une RFC en anglais décrivant le protocole de communication et le comportement souhaité pour l'échange de ressources entre différents proxys. Une mini RFC étendant le protocole HTTP 1.1 afin d'inclure une indication de débit d'émission à respecter pour un serveur HTTP devra également être écrite.

## Projet ##
Le proxy devra être implanté en Java avec l'ensemble des fonctionnalités demandées avec au moins le support d'IPv4. Il est possible d'imaginer de nombreuses améliorations (mode hors-ligne, proxy inversé...) mais celles-ci ne doivent être envisagées que si les fonctionnalités de base sont supportées. Le code source doit être propre, orienté objet et bien documenté (afin de pouvoir générer une Javadoc exploitable). L'utilisation de code ou bibliothèque externe n'est pas autorisée. Le projet doit être accompagné d'un fichier README en anglais décrivant l'utilisation du proxy. Une documentation de développement (en anglais ou en français) décrivant l'architecture du projet, les difficultés rencontrées ainsi que la répartition des tâches devra être présente. Un fichier _build.xml_ à la racine du répertoire du projet permettra de compiler, créér un jar et générer la Javadoc.

Des tests automatisés étant réalisés sur les projets, le proxy devra pouvoir être demarré par la commande _java -jar lib/qroxy.jar -c config ip:port_, _config_ étant le chemin vers le fichier de configuration, ip désignant l'adresse d'attache du proxy et port le port d'attache. Un répertoire conf devra être présent à la racine du projet avec quelques exemples de fichiers de configuration commentés directement utilisables.

Pour tester les fonctionnalités de partage de cache, vous devrez utiliser sur le réseau universitaire des adresses de multicast choisies dans la plage 239.252.0.0-239.255.255.255 avec un TTL fixé à 1.

## Calendrier ##
  * Vendredi 13 avril 2012 à 19h : date limite (avant Toronto) pour la constitution des binômes réalisant le projet (un trinôme autorisé ssi nombre impair d'étudiants)
  * Mardi 8 mai 2012 à 19h : date limite pour le rendu des RFCs
  * Vendredi 1er juin 2012 à 19h : date limite pour le rendu des projets complets par courrier électronique dans une archive .zip jointe avec pour sujet _[Qroxy-IR2] login1 login2_
  * Mercredi 6 juin 2012 de 13h45 à 18h : soutenances des projets
Le choix des binômes, le rendu des RFCs et projets se fait par email adressé à etienne.duris AT univ-mlv.fr et michel.chilowicz AT univ-mlv.fr depuis votre adresse login@etudiant.univ-mlv.fr.