package com.reactive.operators.time;

import com.reactive.core.Disposable;
import com.reactive.core.Observable;
import com.reactive.core.Observer;
import com.reactive.core.Scheduler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Operador sample (throttleLast): emite el último elemento recibido en cada intervalo periódico.
 * 
 * <p>Útil para:
 * <ul>
 *   <li>Reducir la frecuencia de actualizaciones de UI</li>
 *   <li>Tomar muestras periódicas de un stream de datos</li>
 *   <li>Limitar la tasa de eventos manteniendo el más reciente</li>
 * </ul>
 * 
 * <p>Ejemplo:
 * Si period = 500ms y se emiten: A(0ms), B(100ms), C(200ms), D(600ms), E(700ms), F(1100ms)
 * Se emitirán: C(500ms), E(1000ms), F(1500ms)
 * 
 * <p>Nota: El último valor se emite en onComplete si no ha sido emitido aún.
 * 
 * @param <T> Tipo de elementos
 * @author Yasmany Ramos García
 */
public class ObservableSample<T> extends Observable<T> {
    
    private final Observable<T> source;
    private final long period;
    private final TimeUnit unit;
    private final Scheduler scheduler;
    
    public ObservableSample(Observable<T> source, long period, TimeUnit unit, Scheduler scheduler) {
        this.source = source;
        this.period = period;
        this.unit = unit;
        this.scheduler = scheduler;
    }
    
    @Override
    public void subscribe(Observer<? super T> observer) {
        AtomicReference<T> latestValue = new AtomicReference<>();
        AtomicBoolean hasValue = new AtomicBoolean(false);
        AtomicReference<Disposable> samplingTimer = new AtomicReference<>();
        AtomicBoolean disposed = new AtomicBoolean(false);
        
        // Programar el timer periódico que emitirá los valores muestreados
        Disposable timer = scheduler.schedulePeriodic(() -> {
            if (!disposed.get() && hasValue.getAndSet(false)) {
                T value = latestValue.get();
                if (value != null) {
                    observer.onNext(value);
                }
            }
        }, period, period, unit);
        
        samplingTimer.set(timer);
        
        source.subscribe(new Observer<T>() {
            @Override
            public void onNext(T value) {
                if (!disposed.get()) {
                    latestValue.set(value);
                    hasValue.set(true);
                }
            }
            
            @Override
            public void onError(Throwable error) {
                if (!disposed.getAndSet(true)) {
                    Disposable timer = samplingTimer.getAndSet(null);
                    if (timer != null) {
                        timer.dispose();
                    }
                    observer.onError(error);
                }
            }
            
            @Override
            public void onComplete() {
                if (!disposed.getAndSet(true)) {
                    Disposable timer = samplingTimer.getAndSet(null);
                    if (timer != null) {
                        timer.dispose();
                    }
                    // Emitir el último valor si existe y no ha sido emitido
                    if (hasValue.get()) {
                        T value = latestValue.get();
                        if (value != null) {
                            observer.onNext(value);
                        }
                    }
                    observer.onComplete();
                }
            }
        });
    }
}
