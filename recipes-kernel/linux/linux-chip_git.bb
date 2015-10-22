DESCRIPTION = "Linux Kernel for C.H.I.P. boards"
SECTION = "kernel"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=d7810fab7487fb0aad327b76f1be7cd7"

COMPATIBLE_MACHINE = "chip"

inherit kernel
require recipes-kernel/linux/linux-dtb.inc

LINUX_VERSION ?= "4.2-rc1"
PV = "${LINUX_VERSION}+git${SRCPV}"

SRCREV ?= "fd2ad2582c7fb4a5fedff5ac19ca37d138df3963"
BRANCH ?= "chip/stable"
SRC_URI += " \
    git://github.com/NextThingCo/CHIP-linux.git;protocol=git;branch=${BRANCH} \
    file://defconfig \
    "
S = "${WORKDIR}/git"
