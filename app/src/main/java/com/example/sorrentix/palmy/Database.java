package com.example.sorrentix.palmy;

import java.util.ArrayList;

/**
 * Created by ALESSANDROSERRAPICA on 21/09/2017.
 */

public class Database {

    public static final ArrayList<String> heartArrL = new ArrayList<String>();
    public static final ArrayList<String> heartArrS = new ArrayList<String>();

    public static final ArrayList<String> headArrL = new ArrayList<String>();
    public static final ArrayList<String> headArrS = new ArrayList<String>();

    public static final ArrayList<String> lifeArrL = new ArrayList<String>();
    public static final ArrayList<String> lifeArrS = new ArrayList<String>();

    public Database() {
        String [] heartLSamples = {"La tua linea del cuore indica che sei un narcisista e","La tua linea del cuore indica che agisci senza pensare alle conseguenze e","La tua linea del cuore indica che mostri scarsa sensibilità e","La tua linea del cuore indica che sei una persona da relazioni durature e","La tua linea del cuore indica che sei una persona romantica, remissiva e","La tua linea del cuore indica che sei una persona fedele e","La tua linea del cuore indica che avrai una vita ricca di sconvolgimenti e","La tua linea del cuore indica che se in un rapporto di coppia preferiresti spezzarti anzichè piegarti e","La tua linea del cuore indica che vivrai un vero amore e","La tua linea del cuore indica che avrai una vita piena di amore e"};
        String [] heartSSamples = {" sei di carattere debole. "," sei una persona negativa. "," hai difficoltà a mettere a tuo agio le persone. "," non riesci a esprimere i tuoi sentimenti. "," ci sono continui cambiamenti nella tua vita sentimentale"," sei una persona socievole. "," sai creare un'atmosfera romantica. "," che sei affabile e hai una vita sentimentale armoniosa. "," sei una persona carismatica. "," sei una persona disposta a sacrificare tutto per amore. "};

        String [] headLSamples = {"La tua linea della testa indica che sei una persona indecisa e","La tua linea della testa indica che sei una persona impulsiva e","La tua linea della testa indica che hai poca cura delle cose e inoltre","La tua linea della testa indica che sei una persona schematica e","La tua linea della testa indica che sei una persona considerata nelle decisioni e","La tua linea della testa indica che spesso pensi troppo e inoltre","La tua linea della testa indica che hai buone capacità analitiche e","La tua linea della testa indica che sei una persona sveglia e","La tua linea della testa indica che sei una persona decisa e inoltre","La tua linea della testa indica che hai una mente libera da preconcetti e"};
        String [] headSSamples = {" hai una fertile immaginazione. "," hai una grande abilità artistica. "," sei una persona creativa. "," ti lasci influenzare dalle emozioni. "," sei gentile e tollerante. "," hai buone capacità interpersonali. "," sei in grado di mostrare la tua abilità in molti ambiti. "," ti dedichi molto alle tue passioni. "," ottieni buoni risultati nelle materie scientifiche e tecnologiche. "," sei una persona molto ideologista. "};

        String [] lifeLSamples = {"La tua linea della vita è quasi inesistente e questo non è un buon segno. Inoltre","La tua linea della vita indica che sei cagionevole e","La tua linea della vita indica che spesso ti accadono incidenti e","La tua linea della vita indica che non sei una persona ricca di energie e inoltre","La tua linea della vita indica che ti stanchi facilmente e","La tua linea della vita indica che godi sempre di ottima salute e","La tua linea della vita indica che sei una persona vigorosa e","La tua linea della vita indica che sei una persona vitale e inoltre","La tua linea della vita indica che guarisci rapidamente dalle malattie e","La tua linea della vita rivela che sei una persona longeva e inoltre"};
        String [] lifeSSamples = {" la tua forma fisica è in declino. "," sei una persona pigra. "," sprechi le tue energie su cose futili. "," sei una persona impegnata. "," facilmente ti separi dalla tua famiglia e dalle tue cose. "," puoi raggiungere diversi traguardi.  "," sei una persona ambiziosa. "," sei una persona idealista. "," sei una persona ottimista e positiva. "," otterrai fame e prestigio durante la tua vita. "};

        int cont=-1;
        for(int i=0; i<500; i++) {
            if(i%50==0)
                cont++;
            heartArrL.add(i,heartLSamples[cont]);
            headArrL.add(i,headLSamples[cont]);
            lifeArrL.add(i,lifeLSamples[cont]);
        }

        cont = 10;
        for(int i=0; i<180; i++) {
            if(i%18==0)
                cont--;
            heartArrS.add(i,heartSSamples[cont]);
            headArrS.add(i,headSSamples[cont]);
            lifeArrS.add(i,lifeSSamples[cont]);
        }




    }
}
