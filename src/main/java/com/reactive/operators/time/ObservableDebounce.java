package com.reactive.operators.time;

import com.reactive.core.Disposable;
import com.reactive.core.Observable;
import com.reactive.core.Observer;
import com.reactive.core.Scheduler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Operador debounce (throttleWithTimeout): solo emite un elemento si ha pasado un tiempo sin que se emita otro.
 * 
 * <p>Este operador es útil cuando quieres esperar a que un stream de eventos "se calme" antes de
 * reaccionar. Cada vez que llega un nuevo elemento, se reinicia el temporizador. Solo se emite
 * el elemento cuando el temporizador expira sin que lleguen nuevos elementos.
 * 
 * <p>Útil para:
 * <ul>
 *   <li>Búsqueda en tiempo real (esperar a que el usuario deje de escribir)</li>
 *   <li>Evitar clicks múltiples y doble-clicks accidentales</li>
 *   <li>Rate limiting de eventos rápidos y consecutivos</li>
 *   <li>Autocompletado y sugerencias mientras se escribe</li>
 *   <li>Validación de formularios después de que el usuario termine de editar</li>
 * </ul>
 * 
 * <p>Ejemplo de comportamiento:
 * <pre>
 * Si timeout = 300ms y se emiten:
 *   A(0ms), B(100ms), C(200ms), D(600ms), E(700ms), F(800ms)
 * Solo se emitirán:
 *   C(500ms) - porque después de C pasaron 300ms sin eventos
 *   F(1100ms) - porque después de F pasaron 300ms sin eventos
 * </pre>
 * 
 * <p>Caso especial: En onComplete se emite el último valor pendiente si existe.
 * 
 * <p>Diferencia con throttleFirst:
 * <ul>
 *   <li><strong>debounce</strong>: Emite el último elemento después de un período de silencio</li>
 *   <li><strong>throttleFirst</strong>: Emite el primer elemento e ignora los siguientes durante un período</li>
 * </ul>
 * 
 * @param <T> Tipo de elementos
 * @author Yasmany Ramos García
 * @see ObservableThrottleFirst
 */
public class ObservableDebounce<T> extends Observable<T> {
    
    private final Observable<T> source;
    private final long timeout;
    private final TimeUnit unit;
    private final Scheduler scheduler;
    
    public ObservableDebounce(Observable<T> source, long timeout, TimeUnit unit, Scheduler scheduler) {
        this.source = source;
        this.timeout = timeout;
        this.unit = unit;
        this.scheduler = scheduler;
    }
    
    @Override
    public void subscribe(Observer<? super T> observer) {
        source.subscribe(new Observer<T>() {
            private final AtomicReference<T> pendingValue = new AtomicReference<>();
            private final AtomicReference<Disposable> pendingTask = new AtomicReference<>();
            private final AtomicBoolean disposed = new AtomicBoolean(false);
            private final AtomicBoolean hasValue = new AtomicBoolean(false);
            
            @Override
            public void onNext(T value) {
                if (disposed.get()) {
                    return;
                }
                
                // Cancelar tarea pendiente si existe
                Disposable currentTask = pendingTask.getAndSet(null);
                if (currentTask != null && !currentTask.isDisposed()) {
                    currentTask.dispose();
                }
                
                // Guardar el nuevo valor
                pendingValue.set(value);
                hasValue.set(true);
                
                // Programar nueva tarea
                Disposable newTask = scheduler.scheduleDirect(() -> {
                    if (!disposed.get() && hasValue.getAndSet(false)) {
                        T valueToEmit = pendingValue.getAndSet(null);
                        if (valueToEmit != null) {
                            observer.onNext(valueToEmit);
                        }
                    }
                }, timeout, unit);
                
                pendingTask.set(newTask);
            }
            
            @Override
            public void onError(Throwable error) {
                if (!disposed.getAndSet(true)) {
                    cancelPendingTask();
                    pendingValue.set(null);
                    hasValue.set(false);
                    observer.onError(error);
                }
            }
            
            @Override
            public void onComplete() {
                if (!disposed.getAndSet(true)) {
                    cancelPendingTask();
                    // Emitir el último valor pendiente si existe
                    if (hasValue.getAndSet(false)) {
                        T finalValue = pendingValue.getAndSet(null);
                        if (finalValue != null) {
                            observer.onNext(finalValue);
                        }
                    }
                    observer.onComplete();
                }
            }
            
            private void cancelPendingTask() {
                Disposable task = pendingTask.getAndSet(null);
                if (task != null && !task.isDisposed()) {
                    task.dispose();
                }
            }
        });
    }
}
