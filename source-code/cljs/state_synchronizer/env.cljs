
(ns state-synchronizer.env
    (:require [state-synchronizer.state :as state]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn sensor-not-mounted?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if no sensor is mounted with the given synchronizer ID.
  ;
  ; @param (keyword) synchronizer-id
  ;
  ; @return (boolean)
  [synchronizer-id]
  (-> state/MOUNTED-SENSORS deref synchronizer-id pos-int? not))
