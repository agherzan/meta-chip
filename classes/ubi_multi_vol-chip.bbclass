inherit image_types

multiubivol_mkfs () {
   local mkubifs_args="$1"
   local additional_mkubifs_args="$2"
   local additional_ubinize_args="$3"
   local name="$4"
   if [ -z "$5" ]; then
       local vname=""
   else
       local vname="_$5"
   fi
   
   echo \[${name}\] >> ubinize${vname}.cfg
   echo ${additional_ubinize_args} >> ubinize${vname}.cfg
   mkfs.ubifs ${additional_mkubifs_args} ${mkubifs_args}
}

multiubivol_ubinize() {
   if [ -z "$1" ]; then
       local vname=""
   else
       local vname="_$1"
   fi
   ubinize -o ${DEPLOY_DIR_IMAGE}/${IMAGE_NAME}${vname}.rootfs.ubi ${UBINIZE_ARGS} ubinize${vname}.cfg

   # Cleanup cfg file
   mv ubinize${vname}.cfg ${DEPLOY_DIR_IMAGE}/

   # Create own symlinks for 'named' volumes
   if [ -n "$vname" ]; then
       cd ${DEPLOY_DIR_IMAGE}
       if [ -e ${IMAGE_NAME}${vname}.rootfs.ubifs ]; then
           ln -sf ${IMAGE_NAME}${vname}.rootfs.ubifs \
              ${IMAGE_LINK_NAME}${vname}.ubifs
       fi
       if [ -e ${IMAGE_NAME}${vname}.rootfs.ubi ]; then
           ln -sf ${IMAGE_NAME}${vname}.rootfs.ubi \
              ${IMAGE_LINK_NAME}${vname}.ubi
       fi
       cd -
   fi
}

IMAGE_CMD_multiubivol () {

     # Split MKUBIFS_ARGS_<name> and UBINIZE_ARGS_<name>
     for name in ${UBIMULTIVOL_BUILD}; do
        eval local mkubifs_args=\"\$MKUBIFS_ARGS_${name}\"
        eval local additional_ubinize_args=\"\$ADDITIONAL_UBINIZE_ARGS_${name}\"
        eval local additional_mkubifs_args=\"\$ADDITIONAL_MKUBIFS_ARGS_${name}\"

        multiubivol_mkfs "${mkubifs_args}" "${additional_mkubifs_args}" "${additional_ubinize_args}" "${name}" "${UBI_VOLNAME}"
    done
    multiubivol_ubinize ${UBI_VOLNAME}
}

IMAGE_DEPENDS_multiubivol = "mtd-utils-native"

# This variable is available to request which values are suitable for IMAGE_FSTYPES
IMAGE_TYPES = " \
    multiubivol \
    "
