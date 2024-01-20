
(ns state-synchronizer.state)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @ignore
;
; @description
; - Stores the IDs of the currently mounted sensors and the amount of their actual instances.
; - Sensors with the same ID are reckoned as instances of the same sensor.
;
; @atom (map)
; {:my-sensor (integer)}
(def MOUNTED-SENSORS (atom {}))
