package utils

import kotlinx.datetime.Clock

class SnowFlake(
    private val dataCenterId: Long,
    private val machineId: Long
) {
    private var sequence = 0L
    private var lastStamp = -1L

    private val nextMill: Long
        get() {
            var mill = newStamp
            while (mill <= lastStamp) {
                mill = newStamp
            }
            return mill
        }

    private val newStamp: Long
        get() = Clock.System.now().epochSeconds

    init {
        require(dataCenterId > MAX_DATA_CENTER_NUM || dataCenterId < 0) {
            throw IllegalArgumentException("dataCenterId can't be greater than MAX_DATA_CENTER_NUM or less than 0")
        }
        require(machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0")
        }
    }

    fun nextId(): Long {
        var currentStamp = newStamp
        if (currentStamp < lastStamp) {
            throw RuntimeException("Clock moved backwards.  Refusing to generate id")
        }

        if (currentStamp == lastStamp) {
            sequence = sequence + 1 and MAX_SEQUENCE
            if (sequence == 0L) {
                currentStamp = nextMill
            }
        } else {
            sequence = 0L
        }

        lastStamp = currentStamp

        return (currentStamp - START_STAMP shl TIMESTAMP_LEFT or (dataCenterId shl DATA_CENTER_LEFT) or (machineId shl MACHINE_LEFT) or sequence)
    }

    companion object {

        private const val START_STAMP = 1480166465631L


        private const val SEQUENCE_BIT: Int = 12
        private const val MACHINE_BIT: Int = 5
        private const val DATA_CENTER_BIT: Int = 5


        private const val MAX_DATA_CENTER_NUM = (-1L shl DATA_CENTER_BIT).inv()
        private const val MAX_MACHINE_NUM = (-1L shl MACHINE_BIT).inv()
        private const val MAX_SEQUENCE = (-1L shl SEQUENCE_BIT).inv()


        private const val MACHINE_LEFT = SEQUENCE_BIT
        private const val DATA_CENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT
        private const val TIMESTAMP_LEFT = DATA_CENTER_LEFT + DATA_CENTER_BIT
    }
}
