import * as THREE from "three";

import {
    GLTFLoader
} from "https://cdn.jsdelivr.net/npm/three@0.186.1/examples/jsm/loaders/GLTFLoader.js";

import {
    OrbitControls
} from "https://cdn.jsdelivr.net/npm/three@0.186.1/examples/jsm/controls/OrbitControls.js";


/* =========================================================
   AETHERLIA - STARTER MONSTERS 3D
========================================================= */


document.addEventListener(
    "DOMContentLoaded",
    () => {


        const cards =
            document.querySelectorAll(
                ".monster-card"
            );


        if (!cards.length) {

            console.error(
                "Không tìm thấy Monster card."
            );

            return;
        }


        const loader =
            new GLTFLoader();


        const scenes = [];


        /* =====================================================
           MODEL MAP
        ====================================================== */

        const MODEL_MAP = {

            spriglet:
                "/models/monsters/spriglet/spriglet.glb",

            pyron:
                "/models/monsters/pyron/pyron.glb",

            aquaff:
                "/models/monsters/aquaff/aquaff.glb"

        };


        /* =====================================================
           NORMALIZE NAME
        ====================================================== */

        function normalizeMonsterName(
            name
        ) {

            return name
                .trim()
                .toLowerCase()
                .replace(/\s+/g, "-");

        }


        /* =====================================================
           GET MODEL URL
        ====================================================== */

        function getModelUrl(
            name
        ) {

            const key =
                normalizeMonsterName(
                    name
                );


            return (
                MODEL_MAP[key]
                ||
                `/models/monsters/${key}/${key}.glb`
            );
        }


        /* =====================================================
           CREATE SCENE
        ====================================================== */

        function createMonsterScene(
            card
        ) {


            const container =
                card.querySelector(
                    ".monster-3d"
                );


            const loading =
                card.querySelector(
                    ".monster-3d-loading"
                );


            if (!container) {
                return null;
            }


            /* =================================================
               SCENE
            ================================================== */

            const scene =
                new THREE.Scene();


            scene.background =
                null;


            /* =================================================
               CAMERA
            ================================================== */

            const camera =
                new THREE.PerspectiveCamera(
                    35,
                    1,
                    0.1,
                    100
                );


            camera.position.set(
                0,
                1.4,
                5
            );


            /* =================================================
               RENDERER
            ================================================== */

            const renderer =
                new THREE.WebGLRenderer({
                    antialias: true,
                    alpha: true
                });


            renderer.setPixelRatio(
                Math.min(
                    window.devicePixelRatio || 1,
                    2
                )
            );


            renderer.setSize(
                container.clientWidth,
                container.clientHeight,
                false
            );


            renderer.outputColorSpace =
                THREE.SRGBColorSpace;


            renderer.shadowMap.enabled =
                true;


            renderer.shadowMap.type =
                THREE.PCFShadowMap;


            container.appendChild(
                renderer.domElement
            );


            /* =================================================
               LIGHT - HEMISPHERE
            ================================================== */

            const hemisphere =
                new THREE.HemisphereLight(
                    0xffffff,
                    0x6b7280,
                    2.8
                );


            scene.add(
                hemisphere
            );


            /* =================================================
               LIGHT - KEY
            ================================================== */

            const keyLight =
                new THREE.DirectionalLight(
                    0xffffff,
                    3.5
                );


            keyLight.position.set(
                -4,
                7,
                6
            );


            keyLight.castShadow =
                true;


            scene.add(
                keyLight
            );


            /* =================================================
               LIGHT - RIM
            ================================================== */

            const rimLight =
                new THREE.DirectionalLight(
                    0x9fd7ff,
                    1.8
                );


            rimLight.position.set(
                5,
                4,
                -4
            );


            scene.add(
                rimLight
            );


            /* =================================================
               MODEL GROUP
            ================================================== */

            const modelGroup =
                new THREE.Group();


            scene.add(
                modelGroup
            );


            /* =================================================
               GROUND SHADOW
            ================================================== */

            const shadow =
                new THREE.Mesh(
                    new THREE.CircleGeometry(
                        1.3,
                        32
                    ),
                    new THREE.MeshBasicMaterial({
                        color: 0x000000,
                        transparent: true,
                        opacity: 0.16
                    })
                );


            shadow.rotation.x =
                -Math.PI / 2;


            shadow.scale.set(
                1.4,
                0.8,
                1
            );


            shadow.position.y =
                0.02;


            scene.add(
                shadow
            );


            /* =================================================
               CONTROLS
            ================================================== */

            const controls =
                new OrbitControls(
                    camera,
                    renderer.domElement
                );


            controls.enableDamping =
                true;


            controls.dampingFactor =
                0.07;


            controls.enablePan =
                false;


            controls.enableZoom =
                true;


            controls.minDistance =
                2.5;


            controls.maxDistance =
                7;


            controls.target.set(
                0,
                1.1,
                0
            );


            controls.autoRotate =
                true;


            controls.autoRotateSpeed =
                1.0;


            /* =================================================
               STATE
            ================================================== */

            const state = {

                mixer:
                    null,

                root:
                    null,

                modelHeight:
                    1,

                modelCenterY:
                    0

            };


            /* =================================================
               LOAD MODEL
            ================================================== */

            const monsterName =
                card.dataset.monsterName
                || "spriglet";


            const modelUrl =
                getModelUrl(
                    monsterName
                );


            loader.load(

                modelUrl,

                (gltf) => {

                    const model =
                        gltf.scene;


                    state.root =
                        model;


                    /* =========================================
                       MODEL SCALE / BOUNDING BOX
                    ========================================== */

                    const box =
                        new THREE.Box3()
                            .setFromObject(
                                model
                            );


                    const size =
                        new THREE.Vector3();


                    const center =
                        new THREE.Vector3();


                    box.getSize(
                        size
                    );


                    box.getCenter(
                        center
                    );


                    const maxSize =
                        Math.max(
                            size.x,
                            size.y,
                            size.z
                        );


                    if (
                        maxSize > 0
                    ) {

                        const targetSize =
                            3.2;


                        const scale =
                            targetSize /
                            maxSize;


                        model.scale.setScalar(
                            scale
                        );

                    }


                    /* =========================================
                       RECENTER MODEL
                    ========================================== */

                    const scaledBox =
                        new THREE.Box3()
                            .setFromObject(
                                model
                            );


                    const scaledCenter =
                        new THREE.Vector3();


                    scaledBox.getCenter(
                        scaledCenter
                    );


                    const scaledSize =
                        new THREE.Vector3();


                    scaledBox.getSize(
                        scaledSize
                    );


                    model.position.x =
                        -scaledCenter.x;


                    model.position.z =
                        -scaledCenter.z;


                    model.position.y =
                        -scaledBox.min.y;


                    state.modelHeight =
                        scaledSize.y;


                    state.modelCenterY =
                        scaledSize.y /
                        2;


                    /* =========================================
                       SHADOW SCALE
                    ========================================== */

                    const shadowSize =
                        Math.max(
                            scaledSize.x,
                            scaledSize.z
                        );


                    shadow.scale.set(
                        shadowSize * 0.75,
                        shadowSize * 0.48,
                        1
                    );


                    /* =========================================
                       SHADOW SETTINGS
                    ========================================== */

                    model.traverse(
                        (
                            object
                        ) => {

                            if (
                                object.isMesh
                            ) {

                                object.castShadow =
                                    true;


                                object.receiveShadow =
                                    true;


                                if (
                                    object.material
                                ) {

                                    object.material
                                        .needsUpdate =
                                        true;
                                }

                            }

                        }
                    );


                    modelGroup.add(
                        model
                    );


                    /* =========================================
                       ANIMATIONS
                    ========================================== */

                    if (
                        gltf.animations
                        &&
                        gltf.animations.length
                    ) {

                        state.mixer =
                            new THREE.AnimationMixer(
                                model
                            );


                        let idleAnimation =
                            gltf.animations.find(
                                (
                                    animation
                                ) =>
                                    animation.name
                                        .toLowerCase()
                                        .includes(
                                            "idle"
                                        )
                            );


                        if (
                            !idleAnimation
                        ) {

                            idleAnimation =
                                gltf.animations[0];

                        }


                        if (
                            idleAnimation
                        ) {

                            const action =
                                state.mixer
                                    .clipAction(
                                        idleAnimation
                                    );


                            action.play();

                        }

                    }


                    /* =========================================
                       HIDE LOADING
                    ========================================== */

                    if (
                        loading
                    ) {

                        loading.style.display =
                            "none";

                    }


                    console.log(
                        `3D loaded: ${monsterName}`
                    );

                },

                (progress) => {

                    /*
                     * Có thể mở rộng thành
                     * progress bar sau này.
                     */

                },

                (error) => {

                    console.error(
                        `Không thể tải model ${monsterName}`,
                        error
                    );


                    if (
                        loading
                    ) {

                        loading.textContent =
                            "Không tải được model 3D";

                    }

                }

            );


            /* =================================================
               RESIZE
            ================================================== */

            function resize() {

                const width =
                    container.clientWidth;

                const height =
                    container.clientHeight;


                if (
                    width <= 0
                    ||
                    height <= 0
                ) {

                    return;
                }


                renderer.setSize(
                    width,
                    height,
                    false
                );


                camera.aspect =
                    width / height;


                camera.updateProjectionMatrix();

            }


            resize();


            const observer =
                new ResizeObserver(
                    () => {
                        resize();
                    }
                );


            observer.observe(
                container
            );


            return {

                scene,
                camera,
                renderer,
                controls,
                mixer:
                    state,

            };

        }


        /* =====================================================
           CREATE ALL MODELS
        ====================================================== */

        cards.forEach(
            (
                card
            ) => {

                const result =
                    createMonsterScene(
                        card
                    );


                if (
                    result
                ) {

                    scenes.push(
                        result
                    );

                }

            }
        );


        /* =====================================================
           CARD INTERACTION
        ====================================================== */

        cards.forEach(
            (
                card
            ) => {


                card.addEventListener(
                    "mouseenter",
                    () => {

                        card.classList.add(
                            "monster-card-hover"
                        );

                    }
                );


                card.addEventListener(
                    "mouseleave",
                    () => {

                        card.classList.remove(
                            "monster-card-hover"
                        );

                    }
                );


                card.addEventListener(
                    "click",
                    (
                        event
                    ) => {

                        if (
                            event.target.closest(
                                "button"
                            )
                        ) {

                            return;

                        }


                        cards.forEach(
                            (
                                otherCard
                            ) => {

                                otherCard.classList.remove(
                                    "monster-selected"
                                );

                            }
                        );


                        card.classList.add(
                            "monster-selected"
                        );

                    }
                );

            }
        );


        /* =====================================================
           ANIMATION LOOP
        ====================================================== */

        const clock =
            new THREE.Clock();


        function animate() {

            requestAnimationFrame(
                animate
            );


            const delta =
                clock.getDelta();


            scenes.forEach(
                (
                    item
                ) => {

                    if (
                        item.mixer.mixer
                    ) {

                        item.mixer.mixer.update(
                            delta
                        );

                    }


                    item.controls.update();


                    item.renderer.render(
                        item.scene,
                        item.camera
                    );

                }
            );

        }


        animate();


    }
);