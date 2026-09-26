import * as THREE from "three";

import {
    GLTFLoader
} from "https://cdn.jsdelivr.net/npm/three@0.186.1/examples/jsm/loaders/GLTFLoader.js";

import {
    OrbitControls
} from "https://cdn.jsdelivr.net/npm/three@0.186.1/examples/jsm/controls/OrbitControls.js";


/* =========================================================
   AETHERLIA - REGISTER 3D
========================================================= */

document.addEventListener(
    "DOMContentLoaded",
    () => {


        const container =
            document.getElementById(
                "monster3d"
            );


        const nameElement =
            document.getElementById(
                "monsterName"
            );


        const typeElement =
            document.getElementById(
                "monsterType"
            );


        if (
            !container
            ||
            !nameElement
            ||
            !typeElement
        ) {

            console.error(
                "Register 3D: Không tìm thấy container."
            );

            return;
        }


        /* =====================================================
           MODEL DATA
        ====================================================== */

        const monsters = [

            {
                name: "Spriglet",
                type: "NATURE",
                url:
                    "/models/monsters/spriglet/spriglet.glb"
            },

            {
                name: "Pyron",
                type: "FIRE",
                url:
                    "/models/monsters/pyron/pyron.glb"
            },

            {
                name: "Aquaff",
                type: "WATER",
                url:
                    "/models/monsters/aquaff/aquaff.glb"
            }

        ];


        /* =====================================================
           SCENE
        ====================================================== */

        const scene =
            new THREE.Scene();


        /* =====================================================
           CAMERA
        ====================================================== */

        const camera =
            new THREE.PerspectiveCamera(
                34,
                1,
                0.1,
                100
            );


        camera.position.set(
            0,
            1.5,
            5
        );


        /* =====================================================
           RENDERER
        ====================================================== */

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


        renderer.outputColorSpace =
            THREE.SRGBColorSpace;


        renderer.shadowMap.enabled =
            true;


        renderer.shadowMap.type =
            THREE.PCFShadowMap;


        container.appendChild(
            renderer.domElement
        );


        /* =====================================================
           LIGHTS
        ====================================================== */

        const hemisphere =
            new THREE.HemisphereLight(
                0xffffff,
                0x64748b,
                2.5
            );


        scene.add(
            hemisphere
        );


        const keyLight =
            new THREE.DirectionalLight(
                0xffffff,
                3.5
            );


        keyLight.position.set(
            -4,
            6,
            5
        );


        keyLight.castShadow =
            true;


        scene.add(
            keyLight
        );


        const fillLight =
            new THREE.DirectionalLight(
                0xa78bfa,
                1.8
            );


        fillLight.position.set(
            4,
            3,
            -4
        );


        scene.add(
            fillLight
        );


        /* =====================================================
           GROUND SHADOW
        ====================================================== */

        const shadow =
            new THREE.Mesh(
                new THREE.CircleGeometry(
                    1.15,
                    32
                ),
                new THREE.MeshBasicMaterial({
                    color: 0x000000,
                    transparent: true,
                    opacity: 0.12
                })
            );


        shadow.rotation.x =
            -Math.PI / 2;


        shadow.position.y =
            0.01;


        scene.add(
            shadow
        );


        /* =====================================================
           CONTROLS
        ====================================================== */

        const controls =
            new OrbitControls(
                camera,
                renderer.domElement
            );


        controls.enableDamping =
            true;


        controls.dampingFactor =
            0.06;


        controls.enablePan =
            false;


        controls.enableZoom =
            true;


        controls.minDistance =
            2.7;


        controls.maxDistance =
            6.5;


        controls.target.set(
            0,
            1.1,
            0
        );


        controls.autoRotate =
            true;


        controls.autoRotateSpeed =
            0.8;


        /* =====================================================
           LOADER
        ====================================================== */

        const loader =
            new GLTFLoader();


        let currentModel =
            null;


        let mixer =
            null;


        let currentIndex =
            0;


        let changeTimer =
            null;


        /* =====================================================
           CLEAR MODEL
        ====================================================== */

        function clearModel() {

            if (!currentModel) {
                return;
            }


            scene.remove(
                currentModel
            );


            currentModel.traverse(
                (
                    object
                ) => {

                    if (
                        object.geometry
                    ) {

                        object.geometry.dispose();

                    }


                    if (
                        object.material
                    ) {

                        if (
                            Array.isArray(
                                object.material
                            )
                        ) {

                            object.material.forEach(
                                (
                                    material
                                ) => {

                                    material.dispose();

                                }
                            );

                        } else {

                            object.material.dispose();

                        }

                    }

                }
            );


            currentModel =
                null;


            mixer =
                null;
        }


        /* =====================================================
           LOAD MODEL
        ====================================================== */

        function loadMonster(
            index
        ) {

            const monster =
                monsters[index];


            nameElement.textContent =
                monster.name;


            typeElement.textContent =
                monster.type;


            loader.load(

                monster.url,

                (gltf) => {

                    clearModel();


                    const model =
                        gltf.scene;


                    currentModel =
                        model;


                    /* =========================================
                       BOUNDING BOX
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


                    /* =========================================
                       NORMALIZE SIZE
                    ========================================== */

                    if (
                        maxSize > 0
                    ) {

                        const scale =
                            3.0 /
                            maxSize;


                        model.scale.setScalar(
                            scale
                        );

                    }


                    /* =========================================
                       RECENTER
                    ========================================== */

                    const newBox =
                        new THREE.Box3()
                            .setFromObject(
                                model
                            );


                    const newCenter =
                        new THREE.Vector3();


                    newBox.getCenter(
                        newCenter
                    );


                    model.position.x =
                        -newCenter.x;


                    model.position.z =
                        -newCenter.z;


                    model.position.y =
                        -newBox.min.y;


                    /* =========================================
                       SHADOW
                    ========================================== */

                    const newSize =
                        new THREE.Vector3();


                    newBox.getSize(
                        newSize
                    );


                    shadow.scale.set(
                        Math.max(
                            newSize.x,
                            newSize.z
                        ) * 0.85,
                        Math.max(
                            newSize.x,
                            newSize.z
                        ) * 0.5,
                        1
                    );


                    /* =========================================
                       MESH
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


                    /* =========================================
                       ANIMATION
                    ========================================== */

                    if (
                        gltf.animations
                        &&
                        gltf.animations.length
                    ) {

                        mixer =
                            new THREE.AnimationMixer(
                                model
                            );


                        let idle =
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
                            !idle
                        ) {

                            idle =
                                gltf.animations[0];

                        }


                        if (
                            idle
                        ) {

                            const action =
                                mixer.clipAction(
                                    idle
                                );


                            action.play();

                        }

                    }


                    scene.add(
                        model
                    );

                },

                undefined,

                (error) => {

                    console.error(
                        `Không thể tải ${monster.name}:`,
                        error
                    );

                }

            );

        }


        /* =====================================================
           CHANGE MONSTER
        ====================================================== */

        function changeMonster() {

            currentIndex =
                (
                    currentIndex + 1
                )
                %
                monsters.length;


            loadMonster(
                currentIndex
            );

        }


        /* =====================================================
           RESIZE
        ====================================================== */

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


        const resizeObserver =
            new ResizeObserver(
                () => {
                    resize();
                }
            );


        resizeObserver.observe(
            container
        );


        /* =====================================================
           INITIAL
        ====================================================== */

        loadMonster(
            currentIndex
        );


        resize();


        /* =====================================================
           AUTO CHANGE
        ====================================================== */

        changeTimer =
            setInterval(
                changeMonster,
                7000
            );


        /* =====================================================
           ANIMATION
        ====================================================== */

        const clock =
            new THREE.Clock();


        function animate() {

            requestAnimationFrame(
                animate
            );


            const delta =
                clock.getDelta();


            if (
                mixer
            ) {

                mixer.update(
                    delta
                );

            }


            controls.update();


            renderer.render(
                scene,
                camera
            );

        }


        animate();


        /* =====================================================
           PAGE DEBUG
        ====================================================== */

        window.AetherliaRegister3D = {

            scene,
            camera,
            renderer,

            monsters,

            changeMonster,

            loadMonster

        };


        console.log(
            "Aetherlia Register 3D đã khởi tạo."
        );

    }
);