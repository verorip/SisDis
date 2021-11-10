#Sistemi Distribuiti e Pervasivi

##Rete di nodi sensore

Il progetto sviluppato ha l'obbiettivo di progettare un sistema di nodi che hanno il compito di raccogliere dati dai nodi sensore e aggregarli.
Il sistema di nodi è auto-gestito, ovvero l'inserimento o la rimozione di un nodo non richiede un intervento di un operatore per aggiornare la rete.
L'amministrazione dei nodi è operata tramite console.


All'intenro di un'area sono piazzati sensori che raccolgono dati, il sistema è progettato per permettere il deploy di nodi intermedi da console. Il sensore incomincia a trasmettere le informazioni al nodo aggregatore più vicino ad esso, se un nuovo nodo che viene inserito nella rete è più vicino il sensore comincia ad inviare i dati a quest'utimo.
L'invio delle informazioni è fatto inoltrando messaggi JSON tramite socket, ogni nodo riceve informazioni da altri nodi o da sensori e aggrega le informazioni fino ad arrivare al nodo *root*
che permette di accedere e consulatre i dati raccolti.


Il nodo *root* tiene una tabella di tutti i nodi attivi nella rete, e quando uno di questi viene aggiunto o rimosso, *root* notifica a tutti i nodi l'id e la posizione del nuovo nodo, in modo che la rete si possa ricalibrare riassegnando i ruoli.


####Possibile sviluppo
Decentralizzare la gestione della rete implementando la tabella del nodo *root* in tutti i nodi e periodicamente i nodi fanno check dei vicini e si aggiornano le informazioni interne della rete. Ogni nodo, inoltre, inoltra la tabella ai vicini.
