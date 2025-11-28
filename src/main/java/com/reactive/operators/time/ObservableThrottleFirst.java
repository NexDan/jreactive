package com.reactive.operators.time;

import com.reactive.core.Disposable;
import com.reactive.core.Observable;
import com.reactive.core.Observer;
import com.reactive.core.Scheduler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Operador throttleFirst: emite el primer elemento y luego ignora elementos por un período.
 * 
 * <p>También conocido como "sample first" o "throttle leading". Este operador toma el primer
 * elemento que llega, lo emite inmediatamente, y luego abre una "ventana de silencio" durante
 * la cual todos los elementos subsecuentes son ignorados. Cuando la ventana expira, el siguiente
 * elemento que llegue será emitido y se abrirá una nueva ventana.
 * 
 * <p>Útil para:
 * <ul>
 *   <li>Prevenir clicks dobles y múltiples en botones de UI</li>
 *   <li>Limitar la tasa de eventos de UI (scroll, resize)</li>
 *   <li>Rate limiting de API calls manteniendo el primer request</li>
 *   <li>Evitar procesamiento redundante de eventos rápidos</li>
 *   <li>Implementar "cooldown" periods para acciones</li>
 * </ul>
 * 
 * <p>Ejemplo de comportamiento:
 * <pre>
 * Si windowDuration = 500ms y se emiten:
 *   A(0ms), B(100ms), C(200ms), D(600ms), E(700ms), F(1200ms)
 * Se emitirán:
 *   A(0ms)    - primer elemento, inicia ventana [0ms-500ms]
 *   D(600ms)  - primer elemento después de ventana, inicia ventana [600ms-1100ms]
 *   F(1200ms) - primer elemento después de ventana, inicia ventana [1200ms-1700ms]
 * Se ignoran: B, C (dentro de primera ventana), E (dentro de segunda ventana)
 * </pre>
 * 
 * <p>Diferencia con debounce:
 * <ul>
 *   <li><strong>throttleFirst</strong>: Emite el primer elemento e ignora los siguientes durante un período</li>
 *   <li><strong>debounce</strong>: Emite el último elemento después de un período de silencio</li>
 * </ul>
 * 
 * @param <T> Tipo de elementos
 * @author Yasmany Ramos García
 * @see ObservableDebounce
 * @see ObservableThrottleLast
 */
public class ObservableThrottleFirst<T> extends Observable<T> {
    
    private final Observable<T> source;
    private final long windowDuration;
    private final TimeUnit unit;
    private final Scheduler scheduler;
    
    public ObservableThrottleFirst(Observable<T> source, long windowDuration, TimeUnit unit, Scheduler scheduler) {
        this.source = source;
        this.windowDuration = windowDuration;
        this.unit = unit;
        this.scheduler = scheduler;
    }
    
    @Override
    public void subscribe(Observer<? super T> observer) {
        source.subscribe(new Observer<T>() {
            private final AtomicBoolean gate = new AtomicBoolean(true);
            private final AtomicReference<Disposable> windowTask = new AtomicReference<>();
            private final AtomicBoolean disposed = new AtomicBoolean(false);
            
            @Override
            public void onNext(T value) {
                if (disposed.get()) {
                    return;
                }
                
                // Si el gate está abierto, emitir y cerrar
                if (gate.compareAndSet(true, false)) {
                    observer.onNext(value);
                    
                    // Programar apertura del gate después de windowDuration
                    Disposable task = scheduler.scheduleDirect(() -> {
                        gate.set(true);
                    }, windowDuration, unit);
                    
                    // Cancelar tarea anterior si existe y guardar la nueva
                    Disposable oldTask = windowTask.getAndSet(task);
                    if (oldTask != null && !oldTask.isDisposed()) {
                        oldTask.dispose();
                    }
                }
                // Si el gate está cerrado, descartar el valor silenciosamente
            }
            
            @Override
            public void onError(Throwable error) {
                if (!disposed.getAndSet(true)) {
                    cancelWindowTask();
                    observer.onError(error);
                }
            }
            
            @Override
            public void onComplete() {
                if (!disposed.getAndSet(true)) {
                    cancelWindowTask();
                    observer.onComplete();
                }
            }
            
            private void cancelWindowTask() {
                Disposable task = windowTask.getAndSet(null);
                if (task != null && !task.isDisposed()) {
                    task.dispose();
                }
            }
        });
    }
}
