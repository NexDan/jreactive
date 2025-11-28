package com.reactive.operators.time;

import com.reactive.core.Observable;
import com.reactive.core.Observer;
import com.reactive.core.Scheduler;
import java.util.concurrent.TimeUnit;

/**
 * Operador throttleLast: alias de sample, emite el último elemento recibido en cada intervalo.
 * 
 * <p>Este operador es funcionalmente idéntico a {@link ObservableSample}. El nombre "throttleLast"
 * se usa comúnmente en algunas librerías de programación reactiva como un alias de "sample".
 * 
 * <p>Útil para:
 * <ul>
 *   <li>Limitar la tasa de emisión tomando el último elemento de cada ventana</li>
 *   <li>Reducir la frecuencia de actualizaciones</li>
 *   <li>Tomar muestras periódicas manteniendo el valor más reciente</li>
 * </ul>
 * 
 * <p>Ejemplo:
 * Si intervalDuration = 500ms y se emiten: A(0ms), B(100ms), C(200ms), D(600ms), E(700ms)
 * Se emitirán: C(500ms), E(1000ms)
 * 
 * @param <T> Tipo de elementos
 * @author Yasmany Ramos García
 * @see ObservableSample
 */
public class ObservableThrottleLast<T> extends Observable<T> {
    
    private final ObservableSample<T> delegate;
    
    public ObservableThrottleLast(Observable<T> source, long intervalDuration, TimeUnit unit, Scheduler scheduler) {
        this.delegate = new ObservableSample<>(source, intervalDuration, unit, scheduler);
    }
    
    @Override
    public void subscribe(Observer<? super T> observer) {
        delegate.subscribe(observer);
    }
}
