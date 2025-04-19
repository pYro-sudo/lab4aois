package by.losik;

import java.util.*;

public class LogicMinimizer {

    static class Term {
        String vars;
        Set<Integer> coveredMinterms;

        Term(String vars, Set<Integer> coveredMinterms) {
            this.vars = vars;
            this.coveredMinterms = coveredMinterms;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Term term)) return false;
            return vars.equals(term.vars);
        }

        @Override
        public int hashCode() {
            return vars.hashCode();
        }
    }

    public static void main(String[] args) {
        System.out.println("Таблица истинности для переноса (P):");
        System.out.println("A\tB\tC\tP");
        System.out.println("-----------------------");

        boolean[] carryResults = new boolean[8];
        boolean[] sumResults = new boolean[8];

        for (int i = 0; i < 8; i++) {
            boolean A = ((i >> 2) & 1) == 1;
            boolean B = ((i >> 1) & 1) == 1;
            boolean Cout = (i & 1) == 1;

            boolean P = (A && B) || (A && Cout) || (B && Cout);
            carryResults[i] = P;

            System.out.println((A ? "1" : "0") + "\t" +
                    (B ? "1" : "0") + "\t" +
                    (Cout ? "1" : "0") + "\t" +
                    (P ? "1" : "0"));
        }

        System.out.println();
        System.out.println("Таблица истинности для суммы (S):");
        System.out.println("A\tB\tC\tS");
        System.out.println("-----------------------");

        for (int i = 0; i < 8; i++) {
            boolean A = ((i >> 2) & 1) == 1;
            boolean B = ((i >> 1) & 1) == 1;
            boolean P0 = (i & 1) == 1;
            boolean P = carryResults[i];

            boolean S = ((A || B || P0) && !P) || (A && B && P0);
            sumResults[i] = S;

            System.out.println((A ? "1" : "0") + "\t" +
                    (B ? "1" : "0") + "\t" +
                    (P0 ? "1" : "0") + "\t" +
                    (S ? "1" : "0"));
        }



        int sizeSum = 0;
        int sizeCarry = 0;
        for(int i =0;i< sumResults.length;++i){
            if(sumResults[i]){
                sizeSum++;
            }
            if(carryResults[i]){
                sizeCarry++;
            }
        }

        int[] sumMinterms = new int[sizeSum];
        int[] carryMinterms = new int[sizeCarry];

        int countSum =0;
        int countCarry = 0;

        for(int i =0;i< sumResults.length;++i){
            if(sumResults[i]){
                sumMinterms[countSum++] = i;
            }
            if(carryResults[i]){
                carryMinterms[countCarry++] = i;
            }
        }

        System.out.println("Минимизация СДНФ для суммы (S):");
        String minSum = minimizeFunction(sumMinterms, 3);
        System.out.println("S = " + minSum);

        System.out.println("\nМинимизация СДНФ для переноса (P):");
        String minCarry = minimizeFunction(carryMinterms, 3);
        System.out.println("P = " + minCarry);
    }

    static String minimizeFunction(int[] minterms, int varCount) {
        if (minterms.length == 0) return "0";
        if (minterms.length == (1 << varCount)) return "1";

        Set<Term> primeImplicants = findPrimeImplicants(minterms, varCount);

        Set<Term> minimalCover = findMinimalCover(minterms, primeImplicants);

        StringBuilder result = new StringBuilder();
        for (Term term : minimalCover) {
            if (result.length() > 0) result.append(" + ");
            result.append(term.vars);
        }

        return result.toString();
    }

    static Set<Term> findPrimeImplicants(int[] minterms, int varCount) {
        Set<Term> primes = new HashSet<>();
        Map<String, Set<Integer>> groups = new HashMap<>();
        char[] vars = {'A', 'B', 'C'};

        for (int m : minterms) {
            String binary = String.format("%" + varCount + "s", Integer.toBinaryString(m))
                    .replace(' ', '0');
            groups.computeIfAbsent(binary, k -> new HashSet<>()).add(m);
        }

        boolean changed;
        do {
            changed = false;
            Map<String, Set<Integer>> newGroups = new HashMap<>();
            Set<String> matched = new HashSet<>();

            for (String term1 : groups.keySet()) {
                for (String term2 : groups.keySet()) {
                    if (term1.equals(term2)) continue;

                    int diffPos = -1;
                    int diffCount = 0;
                    for (int i = 0; i < term1.length(); i++) {
                        if (term1.charAt(i) != term2.charAt(i)) {
                            diffPos = i;
                            diffCount++;
                        }
                    }

                    if (diffCount == 1) {
                        String newTerm = term1.substring(0, diffPos) + "-" + term1.substring(diffPos + 1);
                        Set<Integer> combined = new HashSet<>(groups.get(term1));
                        combined.addAll(groups.get(term2));
                        newGroups.put(newTerm, combined);
                        matched.add(term1);
                        matched.add(term2);
                        changed = true;
                    }
                }
            }

            for (String term : groups.keySet()) {
                if (!matched.contains(term)) {
                    StringBuilder varsStr = new StringBuilder();
                    for (int i = 0; i < term.length(); i++) {
                        char c = term.charAt(i);
                        if (c == '1') varsStr.append(vars[i]);
                        else if (c == '0') varsStr.append(vars[i]).append("'");
                    }
                    primes.add(new Term(varsStr.toString(), groups.get(term)));
                }
            }

            groups = newGroups;
        } while (changed);

        return primes;
    }

    static Set<Term> findMinimalCover(int[] minterms, Set<Term> primeImplicants) {
        Set<Integer> remainingMinterms = new HashSet<>();
        for (int m : minterms) remainingMinterms.add(m);

        Set<Term> essentialPrimes = new HashSet<>();

        for (Term prime : primeImplicants) {
            for (int m : prime.coveredMinterms) {
                boolean isEssential = true;
                for (Term other : primeImplicants) {
                    if (other != prime && other.coveredMinterms.contains(m)) {
                        isEssential = false;
                        break;
                    }
                }
                if (isEssential) {
                    essentialPrimes.add(prime);
                    break;
                }
            }
        }

        Set<Integer> covered = new HashSet<>();
        for (Term prime : essentialPrimes) {
            covered.addAll(prime.coveredMinterms);
        }

        Set<Term> minimalCover = new HashSet<>(essentialPrimes);
        remainingMinterms.removeAll(covered);

        while (!remainingMinterms.isEmpty()) {
            Term bestPrime = null;
            int maxCover = 0;

            for (Term prime : primeImplicants) {
                if (minimalCover.contains(prime)) continue;

                int currentCover = 0;
                for (int m : prime.coveredMinterms) {
                    if (remainingMinterms.contains(m)) currentCover++;
                }

                if (currentCover > maxCover) {
                    maxCover = currentCover;
                    bestPrime = prime;
                }
            }

            if (bestPrime != null) {
                minimalCover.add(bestPrime);
                remainingMinterms.removeAll(bestPrime.coveredMinterms);
            } else {
                break;
            }
        }

        return minimalCover;
    }
}

