package interpreteur.ast.buildingBlocs.expressions;

import interpreteur.as.Objets.ASObjet;
import interpreteur.as.erreurs.ASErreur;
import interpreteur.ast.buildingBlocs.Expression;

import java.util.function.BiFunction;


public class BinComp implements Expression<ASObjet.Booleen> {
    private final Expression<?> gauche, droite;
    private final Comparateur op;

    public BinComp(Expression<?> gauche, Comparateur op, Expression<?> droite) {
        this.gauche = gauche;
        this.droite = droite;
        this.op = op;
    }

    @Override
    public ASObjet.Booleen eval() {
        return this.op.apply(this.gauche, this.droite);
    }

    @Override
    public String toString() {
        return "BinaryOp{" +
                "gauche=" + gauche +
                ", droite=" + droite +
                ", op=" + op +
                '}';
    }

    public enum Comparateur {
        /**
         * Gere x == y
         */
        EGAL((gauche, droite) -> {
            if (gauche.getValue() == null && droite.getValue() == null) return new ASObjet.Booleen(true);
            return new ASObjet.Booleen(gauche.getValue().equals(droite.getValue()));
        }),

        /**
         * Gere x != y
         */
        PAS_EGAL((gauche, droite) -> {
            if (gauche.getValue() == null && droite.getValue() == null) return new ASObjet.Booleen(true);
            return new ASObjet.Booleen(!gauche.getValue().equals(droite.getValue()));
        }),

        /**
         * Gere x > y
         */
        PLUS_GRAND((gauche, droite) -> {
            if (!(gauche.getValue() instanceof Number || droite.getValue() instanceof Number)) {
                throw new ASErreur.ErreurComparaison("Il est impossible de comparer autre chose que des nombres");
            }
            return new ASObjet.Booleen(((Number) gauche.getValue()).doubleValue() > ((Number) droite.getValue()).doubleValue());
        }),

        /**
         * Gere x < y
         */
        PLUS_PETIT((gauche, droite) -> {
            if (!(gauche.getValue() instanceof Number || droite.getValue() instanceof Number)) {
                throw new ASErreur.ErreurComparaison("Il est impossible de comparer autre chose que des nombres");
            }
            return new ASObjet.Booleen(((Number) gauche.getValue()).doubleValue() < ((Number) droite.getValue()).doubleValue());
        }),

        /**
         * Gere x >= y
         */
        PLUS_GRAND_EGAL((gauche, droite) -> {
            if (!(gauche.getValue() instanceof Number || droite.getValue() instanceof Number)) {
                throw new ASErreur.ErreurComparaison("Il est impossible de comparer autre chose que des nombres");
            }
            return new ASObjet.Booleen(((Number) gauche.getValue()).doubleValue() >= ((Number) droite.getValue()).doubleValue());
        }),

        /**
         * Gere x <= y
         */
        PLUS_PETIT_EGAL((gauche, droite) -> {
            if (!(gauche.getValue() instanceof Number || droite.getValue() instanceof Number)) {
                throw new ASErreur.ErreurComparaison("Il est impossible de comparer autre chose que des nombres");
            }
            return new ASObjet.Booleen(((Number) gauche.getValue()).doubleValue() <= ((Number) droite.getValue()).doubleValue());
        }),

        DANS((gauche, droite) -> {
            if (!(droite.getValue() instanceof ASObjet.Iterable)) {
                throw new ASErreur.ErreurComparaison("L'op\u00E9rateur 'dans' ne s'applique que sur les \u00E9l\u00E9ments de type 'iterable'");
            }
            return new ASObjet.Booleen(((ASObjet.Iterable) droite).contient(gauche));
        }),

        PAS_DANS((gauche, droite) -> {
            if (!(droite.getValue() instanceof ASObjet.Iterable)) {
                throw new ASErreur.ErreurComparaison("L'op\u00E9rateur 'dans' ne s'applique que sur les \u00E9l\u00E9ments de type 'iterable'");
            }
            return new ASObjet.Booleen(!((ASObjet.Iterable) droite).contient(gauche));
        });

        private final BiFunction<ASObjet<?>, ASObjet<?>, ASObjet.Booleen> eval;

        Comparateur(BiFunction<ASObjet<?>, ASObjet<?>, ASObjet.Booleen> eval) {
            this.eval = eval;
        }

        public ASObjet.Booleen apply(Expression<?> gauche, Expression<?> droite) {
            ASObjet<?> g = gauche.eval();
            ASObjet<?> d = droite.eval();
            return this.eval.apply(g, d);
        }
    }
}
