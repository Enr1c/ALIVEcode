package interpreteur.as.modules;

import interpreteur.as.erreurs.ASErreur;
import interpreteur.as.Objets.ASObjet;
import interpreteur.ast.buildingBlocs.expressions.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ModuleNombreUtils extends ASModule {

    public static List<ASObjet.Fonction> fonctions = Arrays.asList(
            new ASObjet.Fonction("entier", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("texte"), "txt", null),
                    new ASObjet.Fonction.Parametre(new Type("entier"), "base", new ASObjet.Entier(10))
            }, new Type("entier")) {
                @Override
                public Entier executer() {
                    String valeur = this.getParamsValeursDict().get("txt").toString();
                    int base = (Integer) this.getParamsValeursDict().get("base").getValue();
                    try {
                        return new Entier(Integer.parseInt(valeur, base));
                    } catch (NumberFormatException ignored) {
                        throw new ASErreur.ErreurType("impossible de convertir '" + valeur + "' en nombre entier de base " + base);
                    }
                }
            },


            new ASObjet.Fonction("decimal", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("texte"), "txt", null)
            }, new Type("decimal")) {
                @Override
                public Decimal executer() {
                    try {
                        return new Decimal(Double.parseDouble(this.getParamsValeursDict().get("txt").toString()));
                    } catch (NumberFormatException ignored) {
                        throw new ASErreur.ErreurType("impossible de convertir '" + this.getParamsValeursDict().get("element").toString() + "' en nombre decimal");
                    }
                }
            },


            new ASObjet.Fonction("nombre", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("texte"), "txt", null)
            }, new Type("decimal")) {
                @Override
                public Nombre executer() {
                    try {
                        String nb = this.getParamsValeursDict().get("txt").toString();
                        boolean estDecimal = nb.contains(".");
                        if (estDecimal) return new Decimal(Double.parseDouble(nb));
                        else return new Entier(Integer.parseInt(nb));
                    } catch (NumberFormatException ignored) {
                        throw new ASErreur.ErreurType("impossible de convertir '" + this.getParamsValeursDict().get("element").toString() + "' en nombre decimal");
                    }
                }
            },


            new ASObjet.Fonction("bin", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("entier"), "nb", null)
            }, new Type("texte")) {
                @Override
                public Texte executer() {
                    return new Texte(Integer.toBinaryString((Integer) this.getValeurParam("nb").getValue()));
                }
            }

    );
    public static List<ASObjet.Constante> constantes = Collections.emptyList();
}

















