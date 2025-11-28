package com.reactive.operators.time;

import com.reactive.core.Disposable;
import com.reactive.core.Observable;
import com.reactive.core.Observer;
import com.reactive.core.Scheduler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Operador delaySubscription: retrasa la suscripción al Observable fuente por un tiempo específico.
 * 
 * <p>A diferencia de {@link ObservableDelay} que retrasa la emisión de cada elemento,
 * delaySubscription retrasa el momento en que se suscribe al Observable fuente.
 * 
 * <p>Útil para:
 * <ul>
 *   <li>Diferir el inicio de una operación costosa</li>
 *   <li>Implementar reintentos con backoff</li>
 *   <li>Coordinar el timing de múltiples streams</li>
 *   <li>Dar tiempo para que se completen operaciones de inicialización</li>
 * </ul>
 * 
 * <p>Ejemplo:
 * Si delay = 1000ms y la fuente emite: A(0ms), B(100ms), C(200ms) después de suscribirse
 * Los elementos se emitirán: A(1000ms), B(1100ms), C(1200ms)
 * (la suscripción se retrasa 1000ms, luego los elementos fluyen normalmente)
 * 
 * <p>Diferencia con delay:
 * <ul>
 *   <li><strong>delaySubscription</strong>: Retrasa cuándo empezar a observar</li>
 *   <li><strong>delay</strong>: Retrasa cada elemento individual</li>
 * </ul>
 * 
 * @param <T> Tipo de elementos
 * @author Yasmany Ramos García
 * @see ObservableDelay
 */
public class ObservableDelaySubscription<T> extends Observable<T> {
    
    private final Observable<T> source;
    private final long delay;
    private final TimeUnit unit;
    private final Scheduler scheduler;
    
    public ObservableDelaySubscription(Observable<T> source, long delay, TimeUnit unit, Scheduler scheduler) {
        this.source = source;
        this.delay = delay;
        this.unit = unit;
        this.scheduler = scheduler;
    }
    
    @Override
    public void subscribe(Observer<? super T> observer) {
        AtomicBoolean disposed = new AtomicBoolean(false);
        AtomicReference<Disposable> delayDisposable = new AtomicReference<>();
        
        // Programar la suscripción retrasada
        Disposable scheduled = scheduler.scheduleDirect(() -> {
            if (!disposed.get()) {
                source.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T value) {
                        if (!disposed.get()) {
                            observer.onNext(value);
                        }
                    }
                    
                    @Override
                    public void onError(Throwable error) {
                        if (!disposed.getAndSet(true)) {
                            observer.onError(error);
                        }
                    }
                    
                    @Override
                    public void onComplete() {
                        if (!disposed.getAndSet(true)) {
                            observer.onComplete();
                        }
                    }
                });
            }
        }, delay, unit);
        
        delayDisposable.set(scheduled);
    }
}
