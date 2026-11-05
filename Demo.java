import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public class Demo {
    static Evento e(int id, int p) { return e(id, p, 0); }
    static Evento e(int id, int p, int dia) {
        return new Evento(id, Instant.parse("2026-01-01T00:00:00Z").plus(Duration.ofDays(dia)),
                          "Evento " + id, p);
    }
    /** n eventos con id 0..499, prioridad 0..9 y fecha en los primeros 30 días de 2026. */
    static Evento[] aleatorios(int n) {
        Random rnd = new Random();
        return IntStream.range(0, n)
            .mapToObj(i -> e(rnd.nextInt(500), rnd.nextInt(10), rnd.nextInt(30)))
            .toArray(Evento[]::new);
    }
    static void verificar(String nombre, Consumer<Evento[]> ordenar,
                          Comparator<Evento> criterio, boolean estable) {
        Random rnd = new Random(2024);
        for (int caso = 0; caso < 250; caso++) {
            int n = caso < 5 ? caso : rnd.nextInt(150);
            Evento[] a = new Evento[n];
            // 30 días posibles: fechas repetidas para probar estabilidad al ordenar por fecha
            for (int i = 0; i < n; i++) a[i] = e(rnd.nextInt(500), rnd.nextInt(10), rnd.nextInt(30));
            if (caso % 3 == 0) Arrays.sort(a, criterio);
            if (caso % 3 == 1) Arrays.sort(a, criterio.reversed());
            Evento[] esperado = a.clone();
            Arrays.sort(esperado, criterio);
            ordenar.accept(a);
            for (int i = 0; i < n; i++) {
                if (criterio.compare(a[i], esperado[i]) != 0)
                    throw new AssertionError(nombre + ": orden incorrecto");
                if (estable && a[i] != esperado[i])
                    throw new AssertionError(nombre + ": estabilidad");
            }
            Set<Evento> originales = Collections.newSetFromMap(new IdentityHashMap<>());
            originales.addAll(Arrays.asList(esperado));
            for (Evento item : a)
                if (!originales.remove(item)) throw new AssertionError("Permutacion");
            if (!originales.isEmpty()) throw new AssertionError("Perdida");
        }
        System.out.println(nombre + ": 250 casos OK");
    }
    public static void main(String[] args) {
        Comparator<Evento> c = Ordenamientos.POR_PRIORIDAD;
        verificar("Bubble", a -> Ordenamientos.bubble(a,c), c, true);
        verificar("Selection", a -> Ordenamientos.selection(a,c), c, false);
        verificar("Insertion", a -> Ordenamientos.insertion(a,c), c, true);
        verificar("Merge", a -> Ordenamientos.merge(a,c), c, true);
        verificar("Quick", a -> Ordenamientos.quick(a,c), c, false);
        verificar("Heap", a -> Ordenamientos.heap(a,c), c, false);
        verificar("Counting", Ordenamientos::counting, c, true);
        verificar("Radix", Ordenamientos::radix,
                  Comparator.comparingInt(Evento::getId), true);
        verificar("Bucket", Ordenamientos::bucket, c, true);
        verificar("Shell", a -> Ordenamientos.shell(a,c), c, false);
        Evento[] extremos = {e(Integer.MAX_VALUE,9), e(0,0), e(10,1)};
        Ordenamientos.radix(extremos);
        if (extremos[2].getId() != Integer.MAX_VALUE) throw new AssertionError();
        Evento[] todosIguales = new Evento[200];
        for (int i=0;i<200;i++) todosIguales[i]=e(i,3);
        Ordenamientos.quick(todosIguales,c);
        Evento[] ejemplo = {e(170,3),e(45,1),e(75,3),e(90,0),e(802,1),e(24,2)};
        Ordenamientos.counting(ejemplo);
        System.out.println(Arrays.toString(ejemplo));

        Comparator<Evento> f = Ordenamientos.POR_FECHA;
        verificar("Insertion por fecha", a -> Ordenamientos.insertion(a,f), f, true);
        verificar("Merge por fecha", a -> Ordenamientos.merge(a,f), f, true);
        verificar("Quick por fecha", a -> Ordenamientos.quick(a,f), f, false);
        if (aleatorios(0).length != 0 || aleatorios(7).length != 7) throw new AssertionError("aleatorios");
        Evento[] porFecha = aleatorios(5);
        Ordenamientos.merge(porFecha, f);
        for (Evento ev : porFecha)
            System.out.println(ev.getId() + " " + ev.getFechaCreacion());
    }
}
