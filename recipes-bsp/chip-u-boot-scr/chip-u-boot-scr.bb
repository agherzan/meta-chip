SUMMARY = "U-boot boot scripts for CHIP boards"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/COPYING.MIT;md5=3da9cfbcb788c80a0384361b4de20420"

inherit deploy

DEPENDS = "u-boot-mkimage-native"

SRC_URI = "file://boot.cmd.in"

NAND_FLASH_START_ADDR = "0x00000000"
SPL_MEMIMG_ADDR = "0x44000000"
UBOOT_MEMIMG_ADDR = "0x4a000000"
SPL_FLASH_UPPER_ADDR = "0x400000"
LED_I2C_CHIP = "0x34"
LED_I2C_ADDR = "0x93"
UBOOT_FLASH_ADDR = "0x800000"
UBI_MEMIMG_ADDR = "0x4b000000"
UBI_FLASH_ADDR = "0x1000000"
OOB_SIZE = "1664"
SCRIPTADDR = "0x43100000"
# max supported image size
UBI_SIZE ?= "0x0F000000"

do_compile[depends] += "u-boot-chip:do_deploy"
do_compile() {
    PADDED_SPL_SIZE_BLOCKS=$(stat --dereference --printf="%s" "${DEPLOY_DIR_IMAGE}/${SPL_ECC_BINARY}")
    PADDED_SPL_SIZE_BLOCKS=$(expr $PADDED_SPL_SIZE_BLOCKS / \( ${CHIP_UBI_PAGE_SIZE} + ${OOB_SIZE} \))
    PADDED_SPL_SIZE_BLOCKS=$(echo $PADDED_SPL_SIZE_BLOCKS | xargs printf "0x%08x")
    PADDED_UBOOT_SIZE=$(stat --dereference  --printf="%s" "${DEPLOY_DIR_IMAGE}/${UBOOT_BINARY}" | xargs printf "0x%08x")

    sed -e "s,@NAND_FLASH_START_ADDR@,${NAND_FLASH_START_ADDR},g" \
        -e "s,@SPL_MEMIMG_ADDR@,${SPL_MEMIMG_ADDR},g" \
        -e "s,@UBOOT_MEMIMG_ADDR@,${UBOOT_MEMIMG_ADDR},g" \
        -e "s,@SPL_FLASH_UPPER_ADDR@,${SPL_FLASH_UPPER_ADDR},g" \
        -e "s,@LED_I2C_CHIP@,${LED_I2C_CHIP},g" \
        -e "s,@LED_I2C_ADDR@,${LED_I2C_ADDR},g" \
        -e "s,@PADDED_SPL_SIZE_BLOCKS@,${PADDED_SPL_SIZE_BLOCKS},g" \
        -e "s,@PADDED_UBOOT_SIZE@,${PADDED_UBOOT_SIZE},g" \
        -e "s,@UBOOT_FLASH_ADDR@,${UBOOT_FLASH_ADDR},g" \
        -e "s,@UBI_FLASH_ADDR@,${UBI_FLASH_ADDR},g" \
        -e "s,@UBI_MEMIMG_ADDR@,${UBI_MEMIMG_ADDR},g" \
        -e "s,@UBI_SIZE@,${UBI_SIZE},g" \
        < "${WORKDIR}/boot.cmd.in" > "${WORKDIR}/boot.cmd"
    mkimage -A arm -T script -C none -n "Boot script" -d "${WORKDIR}/boot.cmd" "${WORKDIR}/boot.scr"
}

do_deploy() {
    install -d ${DEPLOYDIR}
    install -m 0644 ${WORKDIR}/boot.scr ${DEPLOYDIR}/boot.scr-${PV}-${PR}
    ln -sf boot.scr-${PV}-${PR} ${DEPLOYDIR}/boot.scr

    cat > ${DEPLOYDIR}/flash_CHIP_board.sh-${PV}-${PR} <<-EOF
	#!/bin/sh
	#
	
	if [ ! -n "\${UBI_IMAGE}" ]; then
	    echo "Error: UBI_IMAGE environment variable unset."
	    echo "Please set UBI_IMAGE to the basename of the root filesystem image to deploy"
	    exit -1
	elif [ ! -e "${DEPLOY_DIR_IMAGE}/\${UBI_IMAGE}" ]; then
	    echo "Error: UBI_IMAGE file \"${DEPLOY_DIR_IMAGE}/\${UBI_IMAGE}\" does not exist."
	    exit -1
	else
	    CURRENT_UBI_SIZE=\$(stat --dereference --printf="%s" ${DEPLOY_DIR_IMAGE}/\${UBI_IMAGE})
	    MAX_UBI_SIZE=\$(printf %d ${UBI_SIZE})
	    if [ \${CURRENT_UBI_SIZE} -gt \${MAX_UBI_SIZE} ]; then
	        echo "Error: UBI_IMAGE file \"${DEPLOY_DIR_IMAGE}/\${UBI_IMAGE}\" is too large."
	        echo "Current file size is \${CURRENT_UBI_SIZE}"
	        echo "Max file size is \${MAX_UBI_SIZE}"
	        exit -1
	    fi
	fi
	
	PADDED_SPL_SIZE_BLOCKS=$(stat --dereference --printf="%s" "${DEPLOY_DIR_IMAGE}/${SPL_ECC_BINARY}")
	${COMPONENTS_DIR}/x86_64/sunxi-tools-native/usr/bin/sunxi-fel spl ${DEPLOY_DIR_IMAGE}/${SPL_BINARY}
	${COMPONENTS_DIR}/x86_64/sunxi-tools-native/usr/bin/sunxi-fel --progress write ${SPL_MEMIMG_ADDR} ${DEPLOY_DIR_IMAGE}/${SPL_ECC_BINARY}
	${COMPONENTS_DIR}/x86_64/sunxi-tools-native/usr/bin/sunxi-fel --progress write ${UBOOT_MEMIMG_ADDR} ${DEPLOY_DIR_IMAGE}/${UBOOT_BINARY}
	${COMPONENTS_DIR}/x86_64/sunxi-tools-native/usr/bin/sunxi-fel --progress write ${SCRIPTADDR} ${DEPLOY_DIR_IMAGE}/boot.scr
	${COMPONENTS_DIR}/x86_64/sunxi-tools-native/usr/bin/sunxi-fel --progress write ${UBI_MEMIMG_ADDR} ${DEPLOY_DIR_IMAGE}/\${UBI_IMAGE}
	${COMPONENTS_DIR}/x86_64/sunxi-tools-native/usr/bin/sunxi-fel exe ${UBOOT_MEMIMG_ADDR}
	EOF
    chmod +x ${DEPLOYDIR}/flash_CHIP_board.sh-${PV}-${PR}

    ln -sf flash_CHIP_board.sh-${PV}-${PR} ${DEPLOYDIR}/flash_CHIP_board.sh
}

addtask do_deploy after do_compile before do_build

COMPATIBLE_MACHINE = "chip"
