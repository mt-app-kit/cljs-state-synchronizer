
(ns state-synchronizer.views
    (:require [reagent.api                     :as reagent]
              [state-synchronizer.side-effects :as side-effects]
              [state-synchronizer.state        :as state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn- sensor-debug
  ; @ignore
  ;
  ; @param (keyword) synchronizer-id
  ; @param (map) synchronizer-props
  ; {}
  [synchronizer-id {:keys [get-monitor-value-f get-trigger-value-f modify-trigger-value-f]}]
  (let [monitor-value  (if get-monitor-value-f    (get-monitor-value-f))
        trigger-value  (if get-trigger-value-f    (get-trigger-value-f))
        modified-value (if modify-trigger-value-f (modify-trigger-value-f trigger-value) trigger-value)]
       [:div [:br] "monitor value:  " (str monitor-value)
             [:br] "trigger value:  " (str trigger-value)
             [:br] "modified value: " (str modified-value)]))

(defn- sensor-lifecycles
  ; @ignore
  ;
  ; @param (keyword) synchronizer-id
  ; @param (map) synchronizer-props
  ; @param (*) trigger-value
  [synchronizer-id synchronizer-props _]
  (reagent/lifecycles {:component-will-unmount (fn [_ _ _] (side-effects/sensor-will-unmount-f synchronizer-id synchronizer-props))
                       :component-did-mount    (fn [_ _ _] (side-effects/sensor-did-mount-f    synchronizer-id synchronizer-props))
                       :component-did-update   (fn [%]     (side-effects/sensor-did-update-f   synchronizer-id %))
                       :reagent-render         (fn [_ _ _] (if (:debug? synchronizer-props)
                                                               [sensor-debug synchronizer-id synchronizer-props]))}))

(defn sensor
  ; @note
  ; This sensor uses reagent lifecycles to react to parameter updates.
  ;
  ; @description
  ; Keeps two different states synchronized, by reacting to the changes of the secondary state
  ; and updating the primary state in case they are not identical.
  ;
  ; @param (keyword) synchronizer-id
  ; @param (map) synchronizer-props
  ; {:autoclear? (boolean)(opt)
  ;   If TRUE, the sensor overwrites the primary state with NIL when its last instance (of the same ID) gets unmounted.
  ;  :debug? (boolean)(opt)
  ;   If TRUE, displays debug content and prints debug messages.
  ;  :get-monitor-value-f (function)
  ;   Must return the synchronized value (read from the primary state).
  ;  :get-trigger-value-f (function)
  ;   Must return the trigger value (read from the secondary state).
  ;  :modify-trigger-value-f (function)(opt)
  ;   For optionally modifying the trigger value before updating the primary state with it.
  ;   Takes the trigger value as parameter.
  ;  :set-primary-state-f (function)
  ;   Takes the (optionally modified) trigger value as parameter.
  ;   Must update the primary state with the provided trigger value.}
  ;
  ; @usage
  ; (def PRIMARY-STATE   (reagent.core/atom {}))
  ; (def SECONDARY-STATE (reagent.core/atom {}))
  ; (defn my-synchronizer
  ;   []
  ;   [sensor :my-synchronizer {:get-monitor-value-f #(deref  PRIMARY-STATE)
  ;                             :get-trigger-value-f #(deref  SECONDARY-STATE)
  ;                             :set-primary-state-f #(reset! PRIMARY-STATE %)}])
  [synchronizer-id {:keys [get-trigger-value-f] :as synchronizer-props}]
  ; Debouncing / avoiding unnecessary rerenderings:
  ; Using a muted render function '(fn [_ _])' prevents the component reacting to updated parameters.
  ; It reacts only when the output of the 'get-trigger-value-f' function changes.
  (fn [_ _]
      (let [trigger-value (if get-trigger-value-f (get-trigger-value-f))]
           [sensor-lifecycles synchronizer-id synchronizer-props trigger-value])))
