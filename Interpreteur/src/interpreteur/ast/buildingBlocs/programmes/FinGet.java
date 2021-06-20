package interpreteur.ast.buildingBlocs.programmes;

import interpreteur.as.erreurs.ASErreur;
import interpreteur.as.Objets.ASObjet;
import interpreteur.ast.buildingBlocs.Programme;
import interpreteur.executeur.Coordonnee;
import interpreteur.executeur.Executeur;
import interpreteur.tokens.Token;

import java.util.List;

public class FinGet extends Programme {

    @Override
    public ASObjet.Nul execute() {
        return new ASObjet.Nul();
    }

    @Override
    public Coordonnee prochaineCoord(Coordonnee coord, List<Token> ligne) {
        String nomFermeture = "get_";
        if (!coord.getScope().startsWith(nomFermeture))
            throw new ASErreur.ErreurFermeture(coord.getScope(), "fin get");
        return new Coordonnee(Executeur.finScope());
    }

    @Override
    public String toString() {
        return "FinFonction";
    }
}