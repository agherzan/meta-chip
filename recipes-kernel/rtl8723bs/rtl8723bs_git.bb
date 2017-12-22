SUMMARY = "BS realtek wifi"

LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://core/rtw_ap.c;beginline=3;endline=12;md5=6590b50a1b188fc7e8837b19153acd3f"

inherit module

SRCREV = "db2c4f61d48fe3b47c167c8bcd722ce83c24aca5"
SRC_URI = "git://github.com/hadess/rtl8723bs.git;protocol=https \
           file://0001-rtl8723bs-add-modules_install-and-correct-depmod.patch \
          "

S = "${WORKDIR}/git"

EXTRA_OEMAKE = "KSRC=${STAGING_KERNEL_DIR} \
                KVER=${KERNEL_VERSION} \
                SUBARCH=${ARCH} \
                ARCH=${ARCH} \
                MODDESTDIR=${D}/lib/modules/${KERNEL_VERSION}/kernel/drivers/net/wireless/ \
               "
PKGV = "${KERNEL_VERSION}"

