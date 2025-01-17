package interpreteur.as.modules;

import interpreteur.as.erreurs.ASErreur;
import interpreteur.as.Objets.ASObjet;
import interpreteur.ast.buildingBlocs.expressions.Type;

import java.util.*;

public class ModuleListeUtils extends ASModule {

    public static List<ASObjet.Fonction> fonctions = Arrays.asList(

            /*
             * sep:
             * 		@param t:
             * 			-> type: texte
             * 			-> valeur par defaut: null (n'en a pas, il est donc obligatoire de lui en donner une lors de l'appel de la fonction)
             *
             * 		@type_retour liste
             *
             * 		@return une liste où chaque élément est la lettre du string passé en paramètre
             */
            new ASObjet.Fonction("sep", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("texte"), "txt", null),
            }, new Type("liste")) {
                @Override
                public ASObjet<?> executer() {
                    Texte texte = (Texte) this.getParamsValeursDict().get("txt");
                    return new Liste(texte.arrayDeLettres());
                }
            },

            /*
             * inverser:
             * 		@param t:
             * 			-> type: lst
             * 			-> valeur par defaut: null (n'en a pas, il est donc obligatoire de lui en donner une lors de l'appel de la fonction)
             *
             * 		@type_retour iterable
             *
             * 		@return un iterable où chaque élément est inversé
             */
            new ASObjet.Fonction("inverser", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("iterable"), "iter", null),
            }, new Type("iterable")) {
                @Override
                public ASObjet<?> executer() {
                    Iterable element = (Iterable) this.getValeurParam("iter");
                    if (element instanceof Liste){
                        Liste newListe = new Liste();
                        for (int i = element.taille() - 1; i >= 0; i--) newListe.ajouterElement(element.get(i));
                        return newListe;
                    } else {
                        StringBuilder inv = new StringBuilder();
                        for (int i = element.taille() - 1; i >= 0; i--) inv.append(element.get(i).toString());
                        return new Texte(inv.toString());
                    }
                }
            },

            /*
             * map:
             * 		@param f:
             * 			-> type: fonction
             * 			-> valeur par defaut: null (n'en a pas, il est donc obligatoire de lui en donner une lors de l'appel de la fonction)
             *
             * 		@param l:
             * 			-> type: liste
             * 			-> valeur par defaut: null (n'en a pas, il est donc obligatoire de lui en donner une lors de l'appel de la fonction)
             *
             * 		@type_retour liste
             *
             * 		@return la liste formee suite a l'application de la fonction sur chaque element de la liste
             */
            new ASObjet.Fonction("map", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("fonction"), "f", null),
                    new ASObjet.Fonction.Parametre(new Type("liste"), "lst", null)
            }, new Type("liste")) {
                @Override
                public ASObjet<?> executer() {
                    Liste liste = (Liste) this.getParamsValeursDict().get("lst");
                    Fonction f = (Fonction) this.getParamsValeursDict().get("f");
                    Liste nouvelleListe = new Liste();
                    for (ASObjet<?> element : liste.getValue()) {
                        nouvelleListe.ajouterElement(f.setParamPuisExecute(new ArrayList<>(Arrays.asList(element))));
                    }

                    return nouvelleListe;
                }
            },

            /*
             * filtrer:
             * 		@param f:
             * 			-> type: fonction (doit retourner un booleen)
             * 			-> valeur par defaut: null (n'en a pas, il est donc obligatoire de lui en donner une lors de l'appel de la fonction)
             *
             * 		@param l:
             * 			-> type: liste
             * 			-> valeur par defaut: null (n'en a pas, il est donc obligatoire de lui en donner une lors de l'appel de la fonction)
             *
             * 		@type_retour liste
             *
             * 		@return la liste formee des elements de la liste initiale pour lesquels la fonction f a retourne vrai
             */
            new ASObjet.Fonction("filtrer", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("fonction"), "f", null),
                    new ASObjet.Fonction.Parametre(new Type("liste"), "lst", null)
            }, new Type("liste")) {
                @Override
                public ASObjet<?> executer() {
                    Liste liste = (Liste) this.getParamsValeursDict().get("lst");
                    Fonction f = (Fonction) this.getParamsValeursDict().get("f");
                    Liste nouvelleListe = new Liste();
                    for (ASObjet<?> element : liste.getValue()) {
                        if ((Boolean) f.setParamPuisExecute(new ArrayList<>(Arrays.asList(element))).getValue())
                            nouvelleListe.ajouterElement(element);
                    }

                    return nouvelleListe;
                }
            },

            /*
             * joindre:
             * 		@param lst:
             * 			-> type: liste
             * 			-> valeur par defaut: null (n'en a pas, il est donc obligatoire de lui en donner une lors de l'appel de la fonction)
             *
             * 		@param separateur:
             * 			-> type: texte
             * 			-> valeur par defaut: " "
             *
             * 		@type_retour texte
             *
             * 		@return le texte forme en joignant chaque elements de la liste initiale avec le separateur entre chaque element
             */
            new ASObjet.Fonction("joindre", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("liste"), "lst", null),
                    new ASObjet.Fonction.Parametre(new Type("texte"), "separateur", new ASObjet.Texte(""))
            }, new Type("texte")) {
                @Override
                public ASObjet<?> executer() {
                    Liste liste = (Liste) this.getParamsValeursDict().get("lst");
                    Texte separateur = (Texte) this.getParamsValeursDict().get("separateur");
                    return new Texte(
                            String.join(
                                    separateur.getValue(),
                                    liste.getValue().stream().map(Object::toString).toArray(String[]::new)
                            )
                    );
                }
            },

            new ASObjet.Fonction("somme", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("liste"), "lst", null)
            }, new Type("nombre")) {
                @Override
                public ASObjet<?> executer() {
                    Liste liste = (Liste) this.getParamsValeursDict().get("lst");
                    double somme = liste.getValue().stream().mapToDouble(e -> ((Number) e.getValue()).doubleValue()).sum();
                    return new Decimal(somme);
                }
            },

            new ASObjet.Fonction("max", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("liste"), "lst", null)
            }, new Type("nombre")) {
                @Override
                public ASObjet<?> executer() {
                    Liste liste = (Liste) this.getParamsValeursDict().get("lst");
                    OptionalDouble somme = liste.getValue().stream().mapToDouble(e -> ((Number) e.getValue()).doubleValue()).max();
                    if (somme.isEmpty()){
                        throw new ASErreur.ErreurComparaison("tous les \u00E9l\u00E9ments de la liste doivent être des nombres pour pouvoir obtenir le maximum");
                    }
                    return new Decimal(somme.getAsDouble());
                }
            },

            new ASObjet.Fonction("min", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("liste"), "lst", null)
            }, new Type("nombre")) {
                @Override
                public ASObjet<?> executer() {
                    Liste liste = (Liste) this.getParamsValeursDict().get("lst");
                    OptionalDouble somme = liste.getValue().stream().mapToDouble(e -> ((Number) e.getValue()).doubleValue()).min();
                    if (somme.isEmpty()){
                        throw new ASErreur.ErreurComparaison("tous les \u00E9l\u00E9ments de la liste doivent être des nombres pour pouvoir obtenir le minimum");
                    }
                    return new Decimal(somme.getAsDouble());
                }
            },

            /*
             * Agit comme un addAll
             */
            new ASObjet.Fonction("unir", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("liste"), "lst1", null),
                    new ASObjet.Fonction.Parametre(new Type("liste"), "lst2", null)
            }, new Type("liste")) {
                @Override
                public ASObjet<?> executer() {
                    Liste liste1 = (Liste) this.getParamsValeursDict().get("lst1");
                    Liste liste2 = (Liste) this.getParamsValeursDict().get("lst2");
                    Liste newListe = new Liste();
                    return newListe.ajouterTout(liste1).ajouterTout(liste2);
                }
            },

            /*
             * tailleDe:
             * 		@param objet:
             * 			-> type: liste ou texte
             * 			-> valeur par defaut: null (n'en a pas, il est donc obligatoire de lui en donner une lors de l'appel de la fonction)
             *
             * 		@type_retour entier
             *
             * 		@return -> si "choix" est de type liste: le nombre d'element dans la liste
             * 				-> si "choix" est de type texte: le nombre de caractere dans le texte
             */
            new ASObjet.Fonction("tailleDe", new ASObjet.Fonction.Parametre[]{
                    new ASObjet.Fonction.Parametre(new Type("iterable"), "iter", null)
            }, new Type("entier")) {
                @Override
                public ASObjet<?> executer() {
                    Object val = this.getParamsValeursDict().get("iter").getValue();
                    return new Entier(val instanceof String ? val.toString().length() : ((ArrayList<?>) val).size());
                }
            });






    public static List<ASObjet.Constante> constantes = Collections.emptyList();
}
