package interpreteur.as.erreurs;

import interpreteur.data_manager.Data;
import interpreteur.executeur.Executeur;

/**
 * Interface des erreurs d'AliveScript
 *
 * @author Mathis
 */
public interface ASErreur {

    class ErreurAliveScript extends RuntimeException {
        private final String nomErreur;

        public ErreurAliveScript(String message, String nomErreur) {
            super(message);
            this.nomErreur = nomErreur;
        }

        public Data getAsData() {
            int ligne = Executeur.getLineFromCoord(Executeur.obtenirCoordRunTime()) + 1;
            return new Data(Data.Id.ERREUR).addParam(nomErreur).addParam(super.getMessage()).addParam(ligne);
        }

        public Data getAsData(int ligne) {
            return new Data(Data.Id.ERREUR).addParam(nomErreur).addParam(super.getMessage()).addParam(ligne);
        }

        public void afficher() {
            int ligne = Executeur.getLineFromCoord(Executeur.obtenirCoordRunTime()) + 1;
            Executeur.ecrire(this.nomErreur + " (à la ligne " + ligne
                    + ") -> " + super.getMessage());
        }

        public void afficher(int ligne) {
            Executeur.ecrire(this.nomErreur + " (à la ligne " + ligne
                    + ") -> " + super.getMessage());
        }

        public String getNomErreur() {
            return nomErreur;
        }
    }

    class Stop extends RuntimeException {
        private final Data data;

        public Stop(Data data) {
            this.data = data;
        }

        public Data getData() {
            return data;
        }
    }

    class StopSendData extends RuntimeException {
        private final String dataString;

        public StopSendData(String dataString) {
            this.dataString = dataString;
        }

        public String getDataString() {
            return dataString;
        }
    }

    class StopGetInfo extends Stop {
        public StopGetInfo(Data data) {
            super(data);
        }
    }

    class StopSetInfo extends Stop {
        public StopSetInfo(Data data) {
            super(data);
        }
    }

    //--------------------------------------Erreurs compilation----------------------------------------------

    class ErreurFermeture extends ErreurAliveScript {

        public ErreurFermeture(String blocActuel) {
            super("le bloc: '" + blocActuel + "' n'a pas été fermé.", "ErreurFermeture");
        }

        public ErreurFermeture(String blocActuel, String mauvaiseFermeture) {
            super("le bloc: '" + blocActuel + "' a été fermé avec '"
                    + mauvaiseFermeture + "' alors qu'il ne peut pas être fermé.", "ErreurFermeture");
        }

        public ErreurFermeture(String blocActuel, String mauvaiseFermeture, String bonneFermeture) {
            super("le bloc: '" + blocActuel + "' a été fermé avec '"
                    + mauvaiseFermeture + "' alors qu'il doit être fermé avec '" + bonneFermeture + "'.", "ErreurFermeture");
        }
    }


    //-----------------------------------------------Erreurs Executions-------------------------------------------

    class ErreurSyntaxe extends ErreurAliveScript {
        public ErreurSyntaxe(String message) {
            super(message, "ErreurSyntaxe");
        }
    }

    class ErreurModule extends ErreurAliveScript {
        public ErreurModule(String message) {
            super(message, "ErreurModule");
        }
    }

    class ErreurAppelFonction extends ErreurAliveScript {
        public ErreurAppelFonction(String message) {
            super(message, "ErreurAppelFonction");
        }

        public ErreurAppelFonction(String nom, String message) {
            super("dans la fonction '" + nom + "': " + message, "ErreurAppelFonction");
        }
    }

    class ErreurInputOutput extends ErreurAliveScript {
        public ErreurInputOutput(String message) {
            super(message, "ErreurInputOutput");
        }
    }

    class ErreurAssignement extends ErreurAliveScript {
        public ErreurAssignement(String message) {
            super(message, "ErreurAssignement");
        }
    }

    class ErreurType extends ErreurAliveScript {
        public ErreurType(String message) {
            super(message, "ErreurType");
        }
    }

    class ErreurIndex extends ErreurAliveScript {
        public ErreurIndex(String message) {
            super(message, "ErreurIndex");
        }
    }

    class ErreurVariableInconnue extends ErreurAliveScript {
        public ErreurVariableInconnue(String message) {
            super(message, "ErreurVariableInconnue");
        }
    }

    class ErreurComparaison extends ErreurAliveScript {
        public ErreurComparaison(String message) {
            super(message, "ErreurComparaison");
        }
    }

    class ErreurFormatage extends ErreurAliveScript {
        public ErreurFormatage(String message) {
            super(message, "ErreurFormatage");
        }
    }

    class ErreurSuite extends ErreurAliveScript {
        public ErreurSuite(String message) {
            super(message, "ErreurSuite");
        }
    }

    class ErreurEntierInvalide extends ErreurAliveScript {
        public ErreurEntierInvalide(String message) {
            super(message, "ErreurEntierInvalide");
        }
    }


    //-------------------------  Erreur de mathématiques  -----------------------------//

    class ErreurArithmetique extends ErreurAliveScript {
        public ErreurArithmetique(String message) {
            super(message, "ErreurArithmetique");
        }
    }

    class ErreurDivisionParZero extends ErreurAliveScript {
        public ErreurDivisionParZero(String message) {
            super(message, "ErreurDivisionParZero");
        }
    }

    class ErreurModuloZero extends ErreurAliveScript {
        public ErreurModuloZero(String message) {
            super(message, "ErreurModuloZero");
        }
    }

    class ErreurZeroExposantZero extends ErreurAliveScript {
        public ErreurZeroExposantZero(String message) {
            super(message, "ErreurExposantZero");
        }
    }


    // ----------------------------------- Alertes ----------------------------------------- //

    class AlerteExecution {
        private final String message;

        public AlerteExecution(String message) {
            this.message = message;
            this.afficher();
        }

        public void afficher() {
            int ligne = Executeur.getLineFromCoord(Executeur.obtenirCoordRunTime()) + 1;
            Executeur.ecrire("Durant l'execution à la ligne " + ligne
                    + " -> " + this.getClass().getSimpleName() + " : " + this.message);
        }
    }


    class AlerteUtiliserBuiltins extends AlerteExecution {
        public AlerteUtiliserBuiltins(String message) {
            super(message);
        }
    }

}





























