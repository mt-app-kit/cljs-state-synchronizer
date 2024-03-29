
(ns state-synchronizer.side-effects
    (:require [reagent.tools.api        :as reagent.tools]
              [state-synchronizer.env   :as env]
              [state-synchronizer.state :as state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn sensor-did-mount-f
  ; @ignore
  ;
  ; @param (keyword) synchronizer-id
  ; @param (map) synchronizer-props
  ; {}
  [synchronizer-id {:keys [get-monitor-value-f get-trigger-value-f modify-trigger-value-f set-primary-state-f] :as synchronizer-props}]
  (let [monitor-value  (if get-monitor-value-f    (get-monitor-value-f))
        trigger-value  (if get-trigger-value-f    (get-trigger-value-f))
        modified-value (if modify-trigger-value-f (modify-trigger-value-f trigger-value) trigger-value)]
       (when (:debug? synchronizer-props)
             (println "--------------------")
             (println "sensor-did-mount:" synchronizer-id)
             (println "trigger-value:"    trigger-value)
             (println "monitor-value:"    monitor-value)
             (println "modified-value:"   modified-value))
       (swap! state/MOUNTED-SENSORS update synchronizer-id inc)

       ; UNTESTED!
       ; This condition is important to avoid unnecessary primary state writes with nil values when mounting (in case the monitor value is also NIL).
       ; Test it and delete this comment!
       (when (not= modified-value monitor-value)
             (if set-primary-state-f (set-primary-state-f modified-value)))))

(defn sensor-did-update-f
  ; @ignore
  ;
  ; @param (keyword) synchronizer-id
  ; @param (Reagent component object) %
  [synchronizer-id %]
  ; When the secondary state gets updated (i.e., the trigger value changes), it checks whether
  ; the trigger value (read from the secondary state) is different from the monitor value (read from the primary state).
  ; If they are different, it updates the primary state with the (optionally modified) trigger value.
  (let [[_ {:keys [get-monitor-value-f modify-trigger-value-f set-primary-state-f] :as synchronizer-props} trigger-value] (reagent.tools/arguments %)]
       (let [monitor-value  (if get-monitor-value-f    (get-monitor-value-f))
             modified-value (if modify-trigger-value-f (modify-trigger-value-f trigger-value) trigger-value)]
            (when (:debug? synchronizer-props)
                  (println "--------------------")
                  (println "sensor-did-update:" synchronizer-id)
                  (println "trigger-value:"     trigger-value)
                  (println "monitor-value:"     monitor-value)
                  (println "modified-value:"    modified-value))
            (when (not= modified-value monitor-value)
                  (if set-primary-state-f (set-primary-state-f modified-value))))))

(defn sensor-will-unmount-f
  ; @ignore
  ;
  ; @param (keyword) synchronizer-id
  ; @param (map) synchronizer-props
  ; {}
  [synchronizer-id {:keys [autoclear? set-primary-state-f] :as synchronizer-props}]
  (when (:debug? synchronizer-props)
        (println "--------------------")
        (println "sensor-will-unmount:" synchronizer-id)
        (println "autoclear?"           autoclear?))
  (swap! state/MOUNTED-SENSORS update synchronizer-id dec)
  (if (and autoclear? (env/sensor-not-mounted? synchronizer-id))
      (if set-primary-state-f (set-primary-state-f nil))))
