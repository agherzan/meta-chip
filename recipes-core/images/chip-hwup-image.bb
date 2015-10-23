# Base this image on core-image-minimal
include recipes-core/images/core-image-minimal.bb

IMAGE_INSTALL += " \
    kernel-image \
    kernel-modules \
    kernel-devicetree \
    "
