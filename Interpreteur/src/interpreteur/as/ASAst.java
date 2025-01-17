package interpreteur.as;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import interpreteur.as.erreurs.ASErreur.*;
import interpreteur.as.Objets.ASObjet.Booleen;
import interpreteur.as.Objets.ASObjet.Decimal;
import interpreteur.as.Objets.ASObjet.Entier;
import interpreteur.as.Objets.ASObjet.FonctionManager;
import interpreteur.as.Objets.ASObjet.Nul;
import interpreteur.as.Objets.ASObjet.Texte;
import interpreteur.as.Objets.ASObjet.VariableManager;
import interpreteur.as.experimental.ASAstExperimental;
import interpreteur.ast.Ast;
import interpreteur.ast.buildingBlocs.Programme;
import interpreteur.ast.buildingBlocs.expressions.Argument;
import interpreteur.ast.buildingBlocs.Expression;
import interpreteur.ast.buildingBlocs.expressions.*;
import interpreteur.ast.buildingBlocs.programmes.*;
import interpreteur.executeur.Coordonnee;
import interpreteur.executeur.Executeur;
import interpreteur.generateurs.ast.AstGenerator;
import interpreteur.tokens.Token;

import javax.lang.model.type.NullType;


/**
 * Les explications vont être rajouté quand j'aurai la motivation de les écrire XD
 *
 * @author Mathis Laroche
 */


public class ASAst extends AstGenerator {
    public ASAst() {
        VariableManager.varDict.putIfAbsent(VariableManager.scopeParDefaut, new Hashtable<>());
        ajouterProgrammes();
        ajouterExpressions();
    }


    protected void ajouterProgrammes() {
        ajouterProgramme("", new Ast<NullType>() {
            @Override
            public NullType apply(List<Object> p) {
                return null;
            }
        });

        ajouterProgramme("UTILISER expression~"
                        + "UTILISER expression LISTE_OUV expression LISTE_FERM",
                new Ast<Utiliser>() {
                    @Override
                    public Utiliser apply(List<Object> p) {
                        if (p.get(1) instanceof ValeurConstante && ((ValeurConstante) p.get(1)).eval() instanceof Texte) {
                            String msg = (String) ((ValeurConstante) p.get(1)).eval().getValue();
                            if (msg.equalsIgnoreCase("experimental")) {
                                Executeur.setAst(new ASAstExperimental());
                                return new Utiliser(new Var("experimental"));
                            } else {
                                throw new ErreurSyntaxe("Les noms de modules ne doivent pas \u00EAtre \u00E9crits avec des \" \" ou des ' '");
                            }
                        }

                        if (p.size() > 2) {
                            Var[] sous_modules;
                            if (p.get(3) instanceof CreerListe.Enumeration) {
                                sous_modules = ((CreerListe.Enumeration) p.get(3)).getExprs().toArray(Var[]::new);
                            } else {
                                sous_modules = new Var[]{(Var) p.get(3)};
                            }
                            return new Utiliser((Var) p.get(1), sous_modules);
                        }
                        return new Utiliser((Var) p.get(1));
                    }
                });

        ajouterProgramme("AFFICHER expression", new Ast<Afficher>() {
            @Override
            public Afficher apply(List<Object> p) {
                return new Afficher((Expression<?>) p.get(1));
            }
        });

        ajouterProgramme("LIRE DANS expression",
                new Ast<Lire>() {
                    @Override
                    public Lire apply(List<Object> p) {
                        System.out.println(p);
                        if (!(p.get(2) instanceof Var)) {
                            throw new ErreurInputOutput("Une variable est attendue apr\u00E8s la commande 'lire dans', mais '" +
                                    p.get(2).getClass().getSimpleName() + "' a \u00E9t\u00E9 trouv\u00E9.");
                        }
                        return new Lire((Var) p.get(2), null);
                    }
                });

        ajouterProgramme("ATTENDRE expression", new Ast<Attendre>() {
            @Override
            public Attendre apply(List<Object> p) {
                return new Attendre((Expression<?>) p.get(1));
            }
        });

        ajouterProgramme("CONSTANTE expression {assignements} expression~"
                        + "CONSTANTE expression DEUX_POINTS expression {assignements} expression~"
                        + "expression DEUX_POINTS expression {assignements} expression~"
                        + "expression {assignements} expression",
                new Ast<Assigner>() {
                    @Override
                    public Assigner apply(List<Object> p) {
                        /*
                         * TODO erreur si c'est pas une Var qui est passé comme expression à gauche de l'assignement
                         */

                        int idxValeur;
                        int idxAssignement;
                        Type type = null;

                        BinOp.Operation op = null;

                        // si le premier mot est "const"
                        if (p.get(0) instanceof Token) {
                            // si la précision du type est présente
                            if (p.size() == 6) {
                                idxValeur = 5;
                                idxAssignement = 4;

                                // si le type précisisé n'est pas un type
                                if (!(p.get(3) instanceof Type))
                                    throw new ErreurType("Dans une d\u00E9claration de constante, les deux points doivent \u00EAtre suivi d'un type valide");

                                type = (Type) p.get(3);
                            }
                            // si la précision du type n'est pas présente
                            else {
                                idxValeur = 3;
                                idxAssignement = 2;
                            }
                            // si on tente de déclarer une constante avec autre chose que = (ex: +=, *=, -=, etc.)
                            if (!((Token) p.get(idxAssignement)).obtenirNom().equals("ASSIGNEMENT")) {
                                throw new ErreurAssignement("Impossible de modifier la valeur d'une constante");
                            }

                            // si la valeur de l'expression est une énumération d'éléments ex: 3, "salut", 4
                            // on forme une liste avec la suite d'éléments
                            if (p.get(idxValeur) instanceof CreerListe.Enumeration)
                                p.set(idxValeur, ((CreerListe.Enumeration) p.get(idxValeur)).build());

                            // on retourne l'objet Assigner
                            return new Assigner((Expression<?>) p.get(1), (Expression<?>) p.get(idxValeur), true, null, type);

                        }
                        // si le premier mot n'est pas "const"
                        else {
                            // si un type est précisé
                            if (p.size() == 5) {
                                idxValeur = 4;
                                idxAssignement = 3;

                                // si le type précisisé n'est pas un type
                                if (!(p.get(2) instanceof Type))
                                    throw new ErreurType("Dans une d\u00E9claration de constante, les deux points doivent \u00EAtre suivi d'un type valide");
                                type = (Type) p.get(2);

                            } else {
                                idxValeur = 2;
                                idxAssignement = 1;
                            }

                            // si on tente d'assigner avec un opérateur spécial (ex: +=, *=, -=, etc.)
                            if (!((Token) p.get(idxAssignement)).obtenirNom().equals("ASSIGNEMENT")) {
                                op = BinOp.Operation.valueOf(((Token) p.get(idxAssignement)).obtenirNom().split("_")[0]);
                            }

                            // si la valeur de l'expression est une énumération d'éléments ex: var = 3, "salut", 4
                            // on forme une liste avec la suite d'éléments
                            if (p.get(idxValeur) instanceof CreerListe.Enumeration)
                                p.set(idxValeur, ((CreerListe.Enumeration) p.get(idxValeur)).build());
                            return new Assigner((Expression<?>) p.get(0), (Expression<?>) p.get(idxValeur), false, op, type);
                        }
                    }
                });

        ajouterProgramme("{methode_moteur} expression~"
                        + "{methode_moteur}",
                new Ast<MethodeMoteur>(0) {
                    @Override
                    public MethodeMoteur apply(List<Object> p) {
                        String nom = ((Token) p.get(0)).obtenirNom();

                        return new MethodeMoteur(nom, p.size() > 1 ? (Expression<?>) p.get(1) : null);
                    }
                });

        ajouterProgramme("STRUCTURE NOM_VARIABLE", new Ast<Programme>() {
            @Override
            public Programme apply(List<Object> p) {
                return new Programme() {
                    @Override
                    public Object execute() {
                        FonctionManager.ajouterStructure(((Token) p.get(1)).obtenirValeur());
                        return null;
                    }

                    @Override
                    public String toString() {
                        return "";
                    }
                };
            }
        });

        ajouterProgramme("FIN STRUCTURE", new Ast<Programme>() {
            @Override
            public Programme apply(List<Object> p) {
                return new Programme() {
                    @Override
                    public Object execute() {
                        FonctionManager.ajouterStructure(((Token) p.get(1)).obtenirValeur());
                        return null;
                    }

                    @Override
                    public String toString() {
                        return "FinStructure";
                    }
                };
            }
        });

        //<-----------------------------------Les getters----------------------------------------->//
        ajouterProgramme("GET NOM_VARIABLE~" +
                        "GET NOM_VARIABLE FLECHE expression",
                new Ast<CreerGetter>() {
                    @Override
                    public CreerGetter apply(List<Object> p) {
                        Type type = new Type("tout");
                        if (p.size() > 2) {
                            if (!(p.get(3) instanceof Type)) {
                                throw new ErreurType("'" + p.get(4) + "' n'est pas un type valide");
                            }
                            type = (Type) p.get(3);
                        }
                        return new CreerGetter(new Var(((Token) p.get(1)).obtenirValeur()), type);
                    }
                });

        ajouterProgramme("FIN GET",
                new Ast<FinGet>() {
                    @Override
                    public FinGet apply(List<Object> p) {
                        return new FinGet();
                    }
                });

        //<-----------------------------------Les setters----------------------------------------->//
        ajouterProgramme("SET NOM_VARIABLE PARENT_OUV NOM_VARIABLE PARENT_FERM~" +
                        "SET NOM_VARIABLE PARENT_OUV NOM_VARIABLE DEUX_POINTS expression PARENT_FERM",
                new Ast<CreerSetter>(new Object[]{"{nom_type_de_donnees}",
                        new Ast<Type>(0) {
                            @Override
                            public Type apply(List<Object> p) {
                                assert p.get(0) instanceof Token;
                                return new Type(((Token) p.get(0)).obtenirValeur());
                            }
                        }}) {
                    @Override
                    public CreerSetter apply(List<Object> p) {
                        Type type = new Type("tout");
                        if (p.size() > 5) {
                            if (!(p.get(5) instanceof Type)) {
                                throw new ErreurType("'" + p.get(5) + "' n'est pas un type valide");
                            }
                            type = (Type) p.get(5);
                        }
                        return new CreerSetter(new Var(((Token) p.get(1)).obtenirValeur()), new Var(((Token) p.get(3)).obtenirValeur()), type);
                    }
                });

        ajouterProgramme("FIN SET",
                new Ast<FinSet>() {
                    @Override
                    public FinSet apply(List<Object> p) {
                        return new FinSet();
                    }
                });

        //<-----------------------------------Les fonctions----------------------------------------->//

        ajouterProgramme("FONCTION expression PARENT_OUV expression PARENT_FERM FLECHE expression~" +
                        "FONCTION expression PARENT_OUV expression PARENT_FERM",
                new Ast<CreerFonction>(
                        new Object[]{"expression DEUX_POINTS expression ASSIGNEMENT expression~"
                                + "expression ASSIGNEMENT expression~"
                                + "expression DEUX_POINTS expression",
                                new Ast<Argument>(11) {
                                    @Override
                                    public Argument apply(List<Object> p) {
                                        Type type = new Type("tout");
                                        Var var;
                                        Expression<?> valParDefaut = null;

                                        if (!(p.get(0) instanceof Var)) {
                                            throw new ErreurSyntaxe("Une d\u00E9claration de fonction doit commencer par une variable");
                                        }
                                        var = (Var) p.get(0);

                                        Token deuxPointsToken = (Token) p.stream()
                                                .filter(t -> t instanceof Token && ((Token) t).obtenirNom().equals("DEUX_POINTS"))
                                                .findFirst()
                                                .orElse(null);
                                        if (deuxPointsToken != null) {
                                            Expression<?> typeObj = (Expression<?>) p.get(p.indexOf(deuxPointsToken) + 1);
                                            if (!(typeObj instanceof Type)) {
                                                String nom;
                                                if (p.get(0) instanceof Var) {
                                                    nom = ((Var) typeObj).getNom();
                                                } else {
                                                    nom = typeObj.eval().toString();
                                                }
                                                throw new ErreurType("Le symbole ':' doit \u00EAtre suivi d'un type valide ('" + nom + "' n'est pas un type valide)");
                                            }
                                            type = (Type) typeObj;
                                        }
                                        Token assignementToken = (Token) p.stream()
                                                .filter(t -> t instanceof Token && ((Token) t).obtenirNom().equals("ASSIGNEMENT"))
                                                .findFirst()
                                                .orElse(null);
                                        if (assignementToken != null) {
                                            valParDefaut = (Expression<?>) p.get(p.indexOf(assignementToken) + 1);
                                        }

                                        return new Argument(var, valParDefaut, type);
                                    }
                                }}
                ) {
                    @Override
                    public CreerFonction apply(List<Object> p) {
                        Argument[] params = new Argument[]{};

                        Type typeRetour = p.get(p.size() - 1) instanceof Type ? (Type) p.get(p.size() - 1) : new Type("tout");

                        if (p.get(p.size() - 1) == null && p.get(3) instanceof Type) {
                            typeRetour = (Type) p.get(3);
                            return new CreerFonction((Var) p.get(1), params, typeRetour);
                        }

                        if (p.get(3) != null && !(p.get(3) instanceof Token)) {
                            if (p.get(3) instanceof CreerListe.Enumeration) {
                                params = ((CreerListe.Enumeration) p.get(3)).getExprs()
                                        .stream()
                                        .map(expr -> expr instanceof Argument ? (Argument) expr : new Argument((Var) expr, null, null))
                                        .toArray(Argument[]::new);
                            } else if (p.get(3) instanceof Argument) {
                                params = new Argument[]{
                                        (Argument) p.get(3)
                                };
                            } else {
                                params = new Argument[]{
                                        new Argument((Var) p.get(3), null, null)
                                };
                            }
                        }

                        return new CreerFonction((Var) p.get(1), params, typeRetour);
                    }
                });

        ajouterProgramme("RETOURNER~" +
                        "RETOURNER expression",
                new Ast<Retourner>() {
                    @Override
                    public Retourner apply(List<Object> p) {
                        return new Retourner(p.size() > 1 ? (Expression<?>) p.get(1) : new ValeurConstante(new Nul()));
                    }
                });


        ajouterProgramme("FIN FONCTION", new Ast<FinFonction>(0) {
            @Override
            public FinFonction apply(List<Object> p) {
                return new FinFonction();
            }
        });


        ajouterProgramme("NOM_VARIABLE expression~"
                        + "{nom_type_de_donnees} expression~"
                        + "NOM_VARIABLE",
                new Ast<Programme>() {
                    @Override
                    public Programme apply(List<Object> p) {
                        Hashtable<String, Ast<?>> astParams = new Hashtable<>();

                        //astParams.put("expression DEUX_POINTS expression", new Ast<Argument>(8){
                        //    @Override
                        //    public Argument apply(List<Object> p) {
                        //        assert p.get(0) instanceof Var : "gauche assignement doit être Var (source: appelFonction dans ASAst)";
                        //
                        //        return new Argument((Var) p.get(0), (Expression<?>) p.get(2), null);
                        //    }
                        //});
                        Expression<?> contenu;
                        CreerListe args;
                        if (p.size() == 2) {
                            contenu = (Expression<?>) p.get(1);

                            args = contenu instanceof CreerListe.Enumeration ?
                                    ((CreerListe.Enumeration) contenu).build() :
                                    new CreerListe(contenu);
                        } else {
                            args = new CreerListe();
                        }

                        final Expression<?> nom = new Var(((Token) p.get(0)).obtenirValeur());

                        return Programme.evalExpression(new AppelFonc(nom, args), p.get(0).toString());
                    }
                });


        //<-----------------------------------Les blocs de code------------------------------------->
        ajouterProgramme("SI expression",
                new Ast<Si>() {
                    @Override
                    public Si apply(List<Object> p) {
                        return new Si((Expression<?>) p.get(1));
                    }
                });


        ajouterProgramme("SINON",
                new Ast<Sinon>(0) {
                    @Override
                    public Sinon apply(List<Object> p) {
                        return new Sinon();
                    }
                });

        ajouterProgramme("FIN SI", new Ast<FinSi>() {
            @Override
            public FinSi apply(List<Object> p) {
                return new FinSi();
            }
        });

        ajouterProgramme("FAIRE",
                new Ast<Programme>() {
                    @Override
                    public Programme apply(List<Object> p) {
                        return new Programme() {
                            @Override
                            public NullType execute() {
                                Executeur.obtenirCoordRunTime().nouveauBloc("faire");
                                return null;
                            }

                            @Override
                            public Coordonnee prochaineCoord(Coordonnee coord, List<Token> ligne) {
                                return Executeur.obtenirCoordRunTime().nouveauBloc("faire");
                            }

                            @Override
                            public String toString() {
                                return "BoucleFaire";
                            }
                        };
                    }
                });

        ajouterProgramme("TANT_QUE expression",
                new Ast<BoucleTantQue>(0) {
                    @Override
                    public BoucleTantQue apply(List<Object> p) {
                        return new BoucleTantQue((Expression<?>) p.get(1));
                    }
                });

        ajouterProgramme("REPETER expression", new Ast<BoucleRepeter>() {
            @Override
            public BoucleRepeter apply(List<Object> p) {
                return new BoucleRepeter((Expression<?>) p.get(1));
            }
        });

        ajouterProgramme("POUR expression DANS expression",
                new Ast<BouclePour>(0) {
                    @Override
                    public BouclePour apply(List<Object> p) {
                        return new BouclePour((Var) p.get(1), (Expression<?>) p.get(3));
                    }
                });

        ajouterProgramme("SORTIR", new Ast<Boucle.Sortir>() {
            @Override
            public Boucle.Sortir apply(List<Object> p) {
                return new Boucle.Sortir();
            }
        });

        ajouterProgramme("CONTINUER", new Ast<Boucle.Continuer>() {
            @Override
            public Boucle.Continuer apply(List<Object> p) {
                return new Boucle.Continuer();
            }
        });

        ajouterProgramme("FIN POUR~"
                        + "FIN TANT_QUE~"
                        + "FIN REPETER",
                new Ast<FinBoucle>() {
                    @Override
                    public FinBoucle apply(List<Object> p) {
                        return new FinBoucle(((Token) p.get(1)).obtenirValeur());
                    }
                });

        ajouterProgramme("expression", new Ast<Programme>() {
            @Override
            public Programme apply(List<Object> p) {
                return Programme.evalExpression((Expression<?>) p.get(0), p.get(0).toString());
            }
        });
        setOrdreProgramme();
    }


    protected void ajouterExpressions() {

        ajouterExpression("NOM_VARIABLE", new Ast<Var>() {
            @Override
            public Var apply(List<Object> p) {
                return new Var(((Token) p.get(0)).obtenirValeur());
            }
        });

        ajouterExpression("{nom_type_de_donnees}",
                new Ast<Type>() {
                    @Override
                    public Type apply(List<Object> p) {
                        return new Type(((Token) p.get(0)).obtenirValeur());
                    }
                });

        ajouterExpression("expression PIPE expression",
                new Ast<Expression<?>>() {
                    @Override
                    public Expression<?> apply(List<Object> p) {
                        if (p.get(0) instanceof CreerListe.Enumeration) {
                        }

                        if (!(p.get(0) instanceof Type && p.get(2) instanceof Type)) {
                            String nom;
                            if (p.get(0) instanceof Var) {
                                nom = ((Var) p.get(0)).getNom();
                            } else if (p.get(2) instanceof Var) {
                                nom = ((Var) p.get(2)).getNom();
                            } else {
                                nom = p.get(0) instanceof Type ? ((Expression<?>) p.get(2)).eval().toString() : ((Expression<?>) p.get(0)).eval().toString();
                            }

                            throw new ErreurType("Le symbole | doit s\u00E9parer deux types valides " +
                                    "('" + nom + "' n'est pas un type valide)");
                        }
                        ((Type) p.get(0)).union((Type) p.get(2));
                        return (Type) p.get(0);
                    }
                });


        //call fonction
        ajouterExpression("expression PARENT_OUV #expression PARENT_FERM~"
                        + "expression PARENT_OUV PARENT_FERM",
                new Ast<AppelFonc>() {
                    @Override
                    public AppelFonc apply(List<Object> p) {
                        if (p.size() == 3) {
                            Expression<?> nom = p.get(0) instanceof Type ? new Var(((Type) p.get(0)).getNom()) : (Expression<?>) p.get(0);
                            return new AppelFonc(nom, new CreerListe());
                        }
                        Hashtable<String, Ast<?>> astParams = new Hashtable<>();

                        //astParams.put("expression DEUX_POINTS expression", new Ast<Argument>(8){
                        //    @Override
                        //    public Argument apply(List<Object> p) {
                        //        assert p.get(0) instanceof Var : "gauche assignement doit être Var (source: appelFonction dans ASAst)";
                        //
                        //        return new Argument((Var) p.get(0), (Expression<?>) p.get(2), null);
                        //    }
                        //});
                        Expression<?> contenu = AstGenerator.evalOneExpr(new ArrayList<>(p.subList(2, p.size() - 1)), astParams);

                        CreerListe args = contenu instanceof CreerListe.Enumeration ?
                                ((CreerListe.Enumeration) contenu).build() :
                                new CreerListe(contenu);


                        final Expression<?> nom;

                        if (p.get(0) instanceof Type) {
                            nom = new Var(((Type) p.get(0)).getNom());
                        } else {
                            nom = (Expression<?>) p.get(0);
                        }

                        return new AppelFonc(nom, args);
                    }
                });


        ajouterExpression("PARENT_OUV #expression PARENT_FERM~"
                        + "PARENT_OUV expression PARENT_FERM",
                new Ast<Expression<?>>() {
                    @Override
                    public Expression<?> apply(List<Object> p) {
                        //if (result instanceof CreerListe.Enumeration) {
                        //    return ((CreerListe.Enumeration) result).build();
                        //}
                        return AstGenerator.evalOneExpr(new ArrayList<>(p.subList(1, p.size() - 1)), null);
                    }
                });

        //ajouterExpression("!expression PIPE #expression PIPE",
        //        new Ast<UnaryOp>(4) {
        //            @Override
        //            public UnaryOp apply(List<Object> p) {
        //                Expression<?> expr = AstGenerator.eval(new ArrayList<>(p.subList(1, p.size() - 1)), null).get(0);
        //                return new UnaryOp(expr, UnaryOp.Operation.ABSOLUE);
        //            }
        //        });

        ajouterExpression("{type_de_donnees}",
                new Ast<ValeurConstante>() {
                    @Override
                    public ValeurConstante apply(List<Object> p) {
                        Token valeur = (Token) p.get(0);
                        String nom = valeur.obtenirNom();
                        return new ValeurConstante(switch (nom) {
                            case "ENTIER" -> new Entier(valeur);
                            case "DECIMAL" -> new Decimal(valeur);
                            case "TEXTE" -> new Texte(valeur);
                            case "BOOLEEN" -> new Booleen(valeur);
                            case "NUL" -> new Nul();
                            default -> throw new ErreurType("Type de donnee invalide");
                        });
                    }
                });

        ajouterExpression("LISTE_OUV #expression TROIS_POINTS #expression LISTE_FERM~"
                        + "LISTE_OUV #expression TROIS_POINTS #expression BOND #expression LISTE_FERM",
                new Ast<Suite>() {
                    /**
                     * {1...10} -> {1, 2, 3, 4, 5, 6, 7, 8, 9, 10}
                     * {"a"..."g"} -> {"a", "b", "c", "d", "e", "f", "g"}
                     * {"A"..."G"} -> {"A", "B", "C", "D", "E", "F", "G"}
                     */
                    @Override
                    public Suite apply(List<Object> p) {

                        int idxTroisPoints = p.indexOf(p.stream().filter(exp -> exp instanceof Token && ((Token) exp).obtenirNom().equals("TROIS_POINTS")).findFirst().orElseThrow());
                        Token bondToken = (Token) p.stream()
                                .filter(exp -> exp instanceof Token && ((Token) exp).obtenirNom().equals("BOND"))
                                .findFirst()
                                .orElse(null);

                        Expression<?> debut = AstGenerator.evalOneExpr(new ArrayList<>(p.subList(1, idxTroisPoints)), null);
                        Expression<?> fin, bond = null;

                        // pas de bond, forme {debut...fin}
                        if (bondToken == null) {
                            fin = AstGenerator.evalOneExpr(new ArrayList<>(p.subList(idxTroisPoints + 1, p.size() - 1)), null);
                        } else {
                            int idxBond = p.indexOf(bondToken);
                            fin = AstGenerator.evalOneExpr(new ArrayList<>(p.subList(idxTroisPoints + 1, idxBond)), null);
                            bond = AstGenerator.evalOneExpr(new ArrayList<>(p.subList(idxBond + 1, p.size() - 1)), null);
                        }

                        return new Suite(debut, fin, bond);
                    }
                });

        ajouterExpression("LISTE_OUV LISTE_FERM~" +
                        "LISTE_OUV #expression LISTE_FERM",
                new Ast<CreerListe>() {
                    @Override
                    public CreerListe apply(List<Object> p) {
                        if (p.size() < 3) return new CreerListe();
                        Expression<?> contenu = AstGenerator.evalOneExpr(new ArrayList<>(p.subList(1, p.size() - 1)), null);
                        if (contenu instanceof CreerListe.Enumeration)
                            return ((CreerListe.Enumeration) contenu).build();
                        return new CreerListe(contenu);
                    }
                });

        ajouterExpression("expression CROCHET_OUV #expression CROCHET_FERM~"
                        + "expression CROCHET_OUV DEUX_POINTS CROCHET_FERM~"
                        + "expression CROCHET_OUV #expression DEUX_POINTS #expression CROCHET_FERM~"
                        + "expression CROCHET_OUV #expression DEUX_POINTS CROCHET_FERM~"
                        + "expression CROCHET_OUV DEUX_POINTS #expression CROCHET_FERM",
                new Ast<CreerListe.SousSection>() {
                    @Override
                    public CreerListe.SousSection apply(List<Object> p) {
                        Token deux_pointsToken = (Token) p.stream()
                                .filter(exp -> exp instanceof Token && ((Token) exp).obtenirNom().equals("DEUX_POINTS"))
                                .findFirst()
                                .orElse(null);

                        // pas de deux points, forme val[idx]
                        if (deux_pointsToken == null) {
                            Expression<?> idx = AstGenerator.evalOneExpr(new ArrayList<>(p.subList(2, p.size() - 1)), null);
                            return new CreerListe.SousSection.IndexSection((Expression<?>) p.get(0), idx);
                        }
                        // deux points, forme val[debut:fin] ou val[:fin] ou val[debut:] ou val[:]
                        else {
                            Expression<?> debut = null, fin = null;
                            int idxDeuxPoints = p.indexOf(deux_pointsToken);
                            // si debut dans sous section
                            if (idxDeuxPoints > 2) {
                                debut = AstGenerator.evalOneExpr(new ArrayList<>(p.subList(2, idxDeuxPoints)), null);
                            }
                            // si fin dans sous section
                            if (idxDeuxPoints < p.size() - 2) {
                                fin = AstGenerator.evalOneExpr(new ArrayList<>(p.subList(idxDeuxPoints + 1, p.size() - 1)), null);
                            }
                            return new CreerListe.SousSection.CreerSousSection((Expression<?>) p.get(0), debut, fin);
                        }
                    }
                });

        ajouterExpression("expression PLUS PLUS~"
                        + "expression MOINS MOINS",
                new Ast<Incrementer>() {
                    @Override
                    public Incrementer apply(List<Object> p) {
                        final byte signe;
                        if (((Token) p.get(1)).obtenirNom().equals("MOINS")) signe = -1;
                        else signe = 1;
                        return new Incrementer((Expression<?>) p.get(0), signe);
                    }
                });


        ajouterExpression("!expression MOINS expression",
                new Ast<UnaryOp>() {
                    @Override
                    public UnaryOp apply(List<Object> p) {
                        return new UnaryOp((Expression<?>) p.get(1), UnaryOp.Operation.NEGATION);
                    }
                });


        ajouterExpression("expression MOD expression", new Ast<BinOp>() {
            @Override
            public BinOp apply(List<Object> p) {
                return new BinOp((Expression<?>) p.get(0), BinOp.Operation.MOD, (Expression<?>) p.get(2));
            }
        });


        ajouterExpression("expression POW expression", new Ast<BinOp>() {
            @Override
            public BinOp apply(List<Object> p) {
                return new BinOp((Expression<?>) p.get(0), BinOp.Operation.POW, (Expression<?>) p.get(2));
            }
        });


        ajouterExpression("expression MUL expression", new Ast<BinOp>() {
            @Override
            public BinOp apply(List<Object> p) {
                return new BinOp((Expression<?>) p.get(0), BinOp.Operation.MUL, (Expression<?>) p.get(2));
            }
        });


        ajouterExpression("expression DIV expression", new Ast<BinOp>() {
            @Override
            public BinOp apply(List<Object> p) {
                return new BinOp((Expression<?>) p.get(0), BinOp.Operation.DIV, (Expression<?>) p.get(2));
            }
        });


        ajouterExpression("expression DIV_ENTIERE expression", new Ast<BinOp>() {
            @Override
            public BinOp apply(List<Object> p) {
                return new BinOp((Expression<?>) p.get(0), BinOp.Operation.DIV_ENTIERE, (Expression<?>) p.get(2));
            }
        });


        ajouterExpression("expression PLUS expression", new Ast<BinOp>() {
            @Override
            public BinOp apply(List<Object> p) {
                return new BinOp((Expression<?>) p.get(0), BinOp.Operation.PLUS, (Expression<?>) p.get(2));
            }
        });

        ajouterExpression("expression MOINS expression", new Ast<BinOp>() {
            @Override
            public BinOp apply(List<Object> p) {
                return new BinOp((Expression<?>) p.get(0), BinOp.Operation.MOINS, (Expression<?>) p.get(2));
            }
        });

        ajouterExpression("expression DANS expression~" +
                "expression PAS DANS expression", new Ast<BinComp>() {
            @Override
            public BinComp apply(List<Object> p) {
                return p.size() == 3 ?
                        new BinComp((Expression<?>) p.get(0), BinComp.Comparateur.DANS, (Expression<?>) p.get(2))
                        :
                        new BinComp((Expression<?>) p.get(0), BinComp.Comparateur.PAS_DANS, (Expression<?>) p.get(3));
            }
        });


        ajouterExpression("expression {comparaison} expression", new Ast<BinComp>() {
            @Override
            public BinComp apply(List<Object> p) {
                return new BinComp(
                        (Expression<?>) p.get(0),
                        BinComp.Comparateur.valueOf(((Token) p.get(1)).obtenirNom()),
                        (Expression<?>) p.get(2));
            }
        });


        ajouterExpression("expression {porte_logique} expression~"
                + "{porte_logique} expression", new Ast<BoolOp>() {
            @Override
            public BoolOp apply(List<Object> p) {
                if (p.size() == 3) {
                    return new BoolOp(
                            (Expression<?>) p.get(0),
                            BoolOp.Operateur.valueOf(((Token) p.get(1)).obtenirNom()),
                            (Expression<?>) p.get(2));
                } else {
                    return new BoolOp((Expression<?>) p.get(1), BoolOp.Operateur.PAS, null);
                }
            }
        });


        ajouterExpression("expression VIRGULE expression~" +
                        "expression VIRGULE",
                new Ast<CreerListe.Enumeration>() {
                    @Override
                    public CreerListe.Enumeration apply(List<Object> p) {
                        if (p.size() == 2) {
                            if (p.get(0) instanceof CreerListe.Enumeration)
                                return (CreerListe.Enumeration) p.get(0);
                            else
                                return new CreerListe.Enumeration((Expression<?>) p.get(0));
                        }

                        Expression<?> valeur = (Expression<?>) p.get(2);
                        if (p.get(2) instanceof CreerListe.Enumeration) {
                            valeur = ((CreerListe.Enumeration) valeur).build();
                        }
                        if (p.get(0) instanceof CreerListe.Enumeration) {
                            ((CreerListe.Enumeration) p.get(0)).add(valeur);
                            return (CreerListe.Enumeration) p.get(0);
                        }

                        return new CreerListe.Enumeration((Expression<?>) p.get(0), valeur);
                    }
                });
        setOrdreExpression();
    }
}

























