package interpreteur.ast.buildingBlocs.programmes;

import interpreteur.as.erreurs.ASErreur;
import interpreteur.as.Objets.ASObjet;
import interpreteur.ast.buildingBlocs.Expression;
import interpreteur.ast.buildingBlocs.Programme;
import interpreteur.ast.buildingBlocs.expressions.*;

import java.util.HashSet;

public class Assigner extends Programme {
    private static final HashSet<CreerSetter> waitingSetters = new HashSet<>();
    private static final HashSet<CreerGetter> waitingGetters = new HashSet<>();

    private final Expression<?> expr;
    private final Expression<?> valeur;
    private final boolean constante;
    private final BinOp.Operation op;
    private final Type type;
    private final Var var;

    public Assigner(Expression<?> expr, Expression<?> valeur, boolean constante, BinOp.Operation op, Type type) {
        // get la variable
        if (expr instanceof Var) var = (Var) expr;
        else if (expr instanceof CreerListe.SousSection && ((CreerListe.SousSection) expr).getExpr() instanceof Var) {
            var = (Var) ((CreerListe.SousSection) expr).getExpr();
        } else {
            /*
            TODO message d'erreur si expr n'est pas var ou expr.getExpr() retourne autre chose qu'une Var
            */
            throw new ASErreur.ErreurSyntaxe("Il est impossible d'assigner \u00E0 autre chose qu'une variable");
        }
        this.expr = expr;
        this.valeur = valeur;
        if (constante && op != null) {
            throw new ASErreur.ErreurAssignement("Il est impossible de modifier la valeur d'une variable constante");
        }
        this.constante = constante;
        this.op = op;
        this.type = type;
        addVariable();
    }

    public static void addWaitingGetter(CreerGetter getter) {
        waitingGetters.add(getter);
    }

    public static void addWaitingSetter(CreerSetter setter) {
        waitingSetters.add(setter);
    }

    private void addVariable() {

        // get l'objet variable s'il existe
        ASObjet.Variable varObj = ASObjet.VariableManager.obtenirVariable(var.getNom(), var.getScope());

        // si la variable existe déjà et que c'est une constante, lance une erreur, car on ne peut pas modifier une constante
        if (varObj instanceof ASObjet.Constante)
            throw new ASErreur.ErreurAssignement("Il est impossible de changer la valeur d'une constante");

        // si le mot "const" est présent dans l'assignement de la variable
        if (constante) {
            // si la variable existait déjà
            if (varObj != null) {
                throw new ASErreur.ErreurAssignement("Impossible de cr\u00E9er la constante, car la variable '" + var.getNom() + "' a d\u00E9j\u00E0 \u00E9t\u00E9 d\u00E9clar\u00E9e");
            }

            // sinon on crée la constante
            else {
                ASObjet.VariableManager.ajouterConstante(new ASObjet.Constante(var.getNom(), null), var.getScope());
            }

        } else {
            // si la variable n'existe pas et qu'elle n'est pas déclarée avec "const", on crée la variable
            if (varObj == null) {
                if (op != null) {
                    throw new ASErreur.ErreurAssignement("La variable '" + var.getNom() + "' n'a pas \u00E9t\u00E9 d\u00E9clar\u00E9e");
                }
                ASObjet.VariableManager.ajouterVariable(new ASObjet.Variable(var.getNom(), null, type == null ? new Type("tout") : type), var.getScope());
            } else if (type != null) {
                throw new ASErreur.ErreurAssignement("La variable " + var.getNom() + " a d\u00E9j\u00E0 \u00E9t\u00E9 d\u00E9clar\u00E9e avec un type");
            }
        }

        // si des setters et des getters attendaient la déclaration de la variable pour pouvoir être attachée à celle-ci, on les attache
        CreerGetter getter = waitingGetters.stream().filter(waitingGetter -> waitingGetter.getVar().equals(var)).findFirst().orElse(null);
        CreerSetter setter = waitingSetters.stream().filter(waitingSetter -> waitingSetter.getVar().equals(var)).findFirst().orElse(null);
        if (getter != null) {
            getter.addGetter();
            waitingGetters.remove(getter);
        }
        if (setter != null) {
            setter.addSetter();
            waitingSetters.remove(setter);
        }
    }

    @Override
    public Object execute() {
        ASObjet.Variable variable = ASObjet.VariableManager.obtenirVariable(var.getNom());
        ASObjet<?> valeur = this.valeur.eval();


        if (expr instanceof CreerListe.SousSection) {
            ASObjet.Liste listeInitial = (ASObjet.Liste) variable.getValeurApresGetter();

            // si l'assignement est de forme
            // var[debut:fin] = valeur
            if (expr instanceof CreerListe.SousSection.CreerSousSection) {
                if (!(valeur instanceof ASObjet.Liste)) {
                    // TODO ERREUR peut pas assigner une sous liste à autre chose qu'à une liste
                    throw new ASErreur.ErreurAssignement("un interval de valeur doit \u00EAtre assign\u00E9 \u00E0 une liste");
                }
                int fin = ((CreerListe.SousSection.CreerSousSection) expr).getFin();
                int debut = ((CreerListe.SousSection.CreerSousSection) expr).getDebut();
                valeur = listeInitial.remplacerRange(debut, fin, (ASObjet.Liste) valeur);
            }
            // si l'assignement est de forme
            // var[idx] = valeur
            else if (expr instanceof CreerListe.SousSection.IndexSection) {
                int idx = ((CreerListe.SousSection.IndexSection) expr).getIdx();
                if (op != null) {
                    valeur = op.apply(expr, new ValeurConstante(valeur));
                }
                valeur = listeInitial.remplacer(idx, valeur);
                variable.changerValeur(valeur);
                return null;
            }
        }

        if (variable.pasInitialisee() && op != null) {
            throw new ASErreur.ErreurAssignement("La variable '" + var.getNom() + "' n'a pas \u00E8t\u00E8 d\u00E8clar\u00E8");
        }

        if (op != null) {
            valeur = op.apply(var, new ValeurConstante(valeur));
        }

        variable.changerValeur(valeur);

        return null;
    }


    @Override
    public String toString() {
        return "Assigner{" +
                "expr=" + expr +
                ", valeur=" + valeur +
                ", constante=" + constante +
                '}';
    }
}



















