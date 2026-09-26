import * as THREE from "three";

/* =========================================================
   AETHERLIA - PLAYABLE 3D MAP
========================================================= */

document.addEventListener("DOMContentLoaded", () => {

    const gameWorld = document.getElementById("gameWorld");
    const world3D = document.getElementById("game-world-3d");
    const player2D = document.getElementById("player");

    if (!gameWorld || !world3D || !player2D) {
        console.error("Aetherlia 3D Map: Không tìm thấy phần tử cần thiết.");
        return;
    }

    let renderer;
    let scene;
    let camera;

    let trainer;

    let animationFrame;

    let previousPlayerX = null;
    let previousPlayerY = null;

    let lastTime = 0;

    const WORLD_WIDTH = 18;
    const WORLD_DEPTH = 10;

    const MAP_WIDTH = 900;
    const MAP_HEIGHT = 500;

    /* =====================================================
       FALLBACK 2D
    ===================================================== */

    function show2DFallback() {

        world3D.style.display = "none";

        player2D.style.opacity = "1";

        const decorations =
            gameWorld.querySelectorAll(
                ".world-decoration, .building"
            );

        decorations.forEach(element => {
            element.style.opacity = "1";
        });

        console.warn(
            "Aetherlia 3D Map: Đang sử dụng bản đồ 2D dự phòng."
        );
    }


    /* =====================================================
       HIDE OLD 2D
    ===================================================== */

    function hide2D() {

        player2D.style.opacity = "0";

        const decorations =
            gameWorld.querySelectorAll(
                ".world-decoration, .building"
            );

        decorations.forEach(element => {
            element.style.opacity = "0";
        });
    }


    /* =====================================================
       CREATE RENDERER
    ===================================================== */

    try {

        renderer = new THREE.WebGLRenderer({
            antialias: true,
            alpha: false
        });

        renderer.setPixelRatio(
            Math.min(window.devicePixelRatio || 1, 2)
        );

        renderer.shadowMap.enabled = true;
        renderer.shadowMap.type = THREE.PCFShadowMap;

        renderer.outputColorSpace = THREE.SRGBColorSpace;

        renderer.setClearColor(
            0x8ecf7d,
            1
        );

        world3D.innerHTML = "";

        world3D.appendChild(
            renderer.domElement
        );

    } catch (error) {

        console.error(
            "Aetherlia 3D Map: Không thể tạo WebGL Renderer.",
            error
        );

        show2DFallback();

        return;
    }


    /* =====================================================
       SCENE
    ===================================================== */

    scene = new THREE.Scene();

    scene.background =
        new THREE.Color(0x8ecf7d);

    scene.fog =
        new THREE.Fog(
            0x8ecf7d,
            18,
            34
        );


    /* =====================================================
       CAMERA
    ===================================================== */

    camera =
        new THREE.PerspectiveCamera(
            45,
            1,
            0.1,
            100
        );

    camera.position.set(
        0,
        13,
        15
    );

    camera.lookAt(
        0,
        0,
        0
    );


    /* =====================================================
       LIGHT
    ===================================================== */

    const ambientLight =
        new THREE.HemisphereLight(
            0xffffff,
            0x52734e,
            2.2
        );

    scene.add(
        ambientLight
    );


    const sun =
        new THREE.DirectionalLight(
            0xffffff,
            3
        );

    sun.position.set(
        -8,
        16,
        8
    );

    sun.castShadow = true;

    sun.shadow.mapSize.width = 2048;
    sun.shadow.mapSize.height = 2048;

    sun.shadow.camera.left = -15;
    sun.shadow.camera.right = 15;
    sun.shadow.camera.top = 15;
    sun.shadow.camera.bottom = -15;

    sun.shadow.camera.near = 0.1;
    sun.shadow.camera.far = 50;

    scene.add(
        sun
    );


    /* =====================================================
       GROUND
    ===================================================== */

    const groundGeometry =
        new THREE.PlaneGeometry(
            WORLD_WIDTH,
            WORLD_DEPTH,
            1,
            1
        );

    const groundMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x7fc86c,
            roughness: 0.95,
            metalness: 0
        });

    const ground =
        new THREE.Mesh(
            groundGeometry,
            groundMaterial
        );

    ground.rotation.x =
        -Math.PI / 2;

    ground.receiveShadow = true;

    scene.add(
        ground
    );


    /* =====================================================
       GROUND INNER AREA
    ===================================================== */

    const innerGeometry =
        new THREE.PlaneGeometry(
            16.7,
            8.7
        );

    const innerMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x91d77d,
            roughness: 1
        });

    const innerGround =
        new THREE.Mesh(
            innerGeometry,
            innerMaterial
        );

    innerGround.rotation.x =
        -Math.PI / 2;

    innerGround.position.y =
        0.01;

    innerGround.receiveShadow = true;

    scene.add(
        innerGround
    );


    /* =====================================================
       MAIN PATH
    ===================================================== */

    const pathMaterial =
        new THREE.MeshStandardMaterial({
            color: 0xc6a879,
            roughness: 1
        });


    const mainPathGeometry =
        new THREE.BoxGeometry(
            3.2,
            0.12,
            8.6
        );

    const mainPath =
        new THREE.Mesh(
            mainPathGeometry,
            pathMaterial
        );

    mainPath.position.set(
        0,
        0.08,
        0.5
    );

    mainPath.receiveShadow = true;

    scene.add(
        mainPath
    );


    /* =====================================================
       HORIZONTAL PATH
    ===================================================== */

    const horizontalPathGeometry =
        new THREE.BoxGeometry(
            16,
            0.12,
            2.1
        );

    const horizontalPath =
        new THREE.Mesh(
            horizontalPathGeometry,
            pathMaterial
        );

    horizontalPath.position.set(
        0,
        0.085,
        0.8
    );

    horizontalPath.receiveShadow = true;

    scene.add(
        horizontalPath
    );


    /* =====================================================
       GRASS ENCOUNTER AREA
    ===================================================== */

    const grassAreaGeometry =
        new THREE.BoxGeometry(
            6.3,
            0.08,
            3.9
        );

    const grassAreaMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x5fa84f,
            roughness: 1
        });

    const grassArea =
        new THREE.Mesh(
            grassAreaGeometry,
            grassAreaMaterial
        );

    grassArea.position.set(
        -5.0,
        0.06,
        2.8
    );

    grassArea.receiveShadow = true;

    scene.add(
        grassArea
    );


    /* =====================================================
       GRASS BLADES
    ===================================================== */

    function createGrassBlade(
        x,
        z,
        scale
    ) {

        const geometry =
            new THREE.ConeGeometry(
                0.08 * scale,
                0.42 * scale,
                4
            );

        const material =
            new THREE.MeshStandardMaterial({
                color: 0x347d31,
                roughness: 1
            });

        const blade =
            new THREE.Mesh(
                geometry,
                material
            );

        blade.position.set(
            x,
            0.27 * scale,
            z
        );

        blade.castShadow = true;

        scene.add(
            blade
        );
    }


    const grassPositions = [
        [-7.0, 2.2],
        [-6.3, 2.7],
        [-5.5, 2.3],
        [-4.7, 2.9],
        [-3.8, 2.5],
        [-7.1, 3.3],
        [-6.0, 3.6],
        [-4.9, 3.4],
        [-3.9, 3.7],
        [-6.8, 4.2],
        [-5.8, 4.3],
        [-4.7, 4.2],
        [-3.6, 4.1]
    ];

    grassPositions.forEach(
        ([x, z], index) => {

            createGrassBlade(
                x,
                z,
                0.8 + (index % 3) * 0.15
            );

        }
    );


    /* =====================================================
       TREE
    ===================================================== */

    function createTree(
        x,
        z,
        scale = 1
    ) {

        const group =
            new THREE.Group();

        group.position.set(
            x,
            0,
            z
        );

        group.scale.setScalar(
            scale
        );


        /* TRUNK */

        const trunkGeometry =
            new THREE.CylinderGeometry(
                0.22,
                0.28,
                1.55,
                8
            );

        const trunkMaterial =
            new THREE.MeshStandardMaterial({
                color: 0x70472a,
                roughness: 1
            });

        const trunk =
            new THREE.Mesh(
                trunkGeometry,
                trunkMaterial
            );

        trunk.position.y =
            0.77;

        trunk.castShadow = true;

        group.add(
            trunk
        );


        /* LEAVES 1 */

        const leafMaterial =
            new THREE.MeshStandardMaterial({
                color: 0x2f8f3b,
                roughness: 0.9
            });


        const leaf1 =
            new THREE.Mesh(
                new THREE.SphereGeometry(
                    0.8,
                    10,
                    8
                ),
                leafMaterial
            );

        leaf1.position.set(
            0,
            1.7,
            0
        );

        leaf1.castShadow = true;

        group.add(
            leaf1
        );


        /* LEAVES 2 */

        const leaf2 =
            new THREE.Mesh(
                new THREE.SphereGeometry(
                    0.68,
                    10,
                    8
                ),
                leafMaterial
            );

        leaf2.position.set(
            -0.55,
            1.45,
            0.05
        );

        leaf2.castShadow = true;

        group.add(
            leaf2
        );


        /* LEAVES 3 */

        const leaf3 =
            new THREE.Mesh(
                new THREE.SphereGeometry(
                    0.68,
                    10,
                    8
                ),
                leafMaterial
            );

        leaf3.position.set(
            0.55,
            1.45,
            0.05
        );

        leaf3.castShadow = true;

        group.add(
            leaf3
        );


        /* SHADOW */

        const shadow =
            new THREE.Mesh(
                new THREE.CircleGeometry(
                    0.8,
                    20
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
            0.03;

        group.add(
            shadow
        );


        scene.add(
            group
        );

        return group;
    }


    /* =====================================================
       TREES
    ===================================================== */

    createTree(
        -7.7,
        -3.5,
        1.35
    );

    createTree(
        -4.6,
        -3.7,
        1.15
    );

    createTree(
        5.8,
        -3.5,
        1.3
    );

    createTree(
        7.5,
        -1.1,
        1.15
    );

    createTree(
        7.1,
        3.5,
        1.3
    );

    createTree(
        4.6,
        3.8,
        1.05
    );

    createTree(
        -7.8,
        0.0,
        1.0
    );


    /* =====================================================
       HOUSE
    ===================================================== */

    function createHouse(
        x,
        z,
        scale = 1
    ) {

        const group =
            new THREE.Group();

        group.position.set(
            x,
            0,
            z
        );

        group.scale.setScalar(
            scale
        );


        /* HOUSE BODY */

        const body =
            new THREE.Mesh(
                new THREE.BoxGeometry(
                    3.0,
                    2.1,
                    2.4
                ),
                new THREE.MeshStandardMaterial({
                    color: 0xf3d6a3,
                    roughness: 0.9
                })
            );

        body.position.y =
            1.05;

        body.castShadow = true;
        body.receiveShadow = true;

        group.add(
            body
        );


        /* ROOF */

        const roof =
            new THREE.Mesh(
                new THREE.ConeGeometry(
                    2.35,
                    1.7,
                    4
                ),
                new THREE.MeshStandardMaterial({
                    color: 0xb33e32,
                    roughness: 0.95
                })
            );

        roof.rotation.y =
            Math.PI / 4;

        roof.position.y =
            2.85;

        roof.castShadow = true;

        group.add(
            roof
        );


        /* DOOR */

        const door =
            new THREE.Mesh(
                new THREE.BoxGeometry(
                    0.65,
                    1.15,
                    0.08
                ),
                new THREE.MeshStandardMaterial({
                    color: 0x673c24,
                    roughness: 1
                })
            );

        door.position.set(
            0,
            0.62,
            1.24
        );

        door.castShadow = true;

        group.add(
            door
        );


        /* WINDOW LEFT */

        const windowMaterial =
            new THREE.MeshStandardMaterial({
                color: 0x8fd6f4,
                roughness: 0.3
            });


        const window1 =
            new THREE.Mesh(
                new THREE.BoxGeometry(
                    0.65,
                    0.55,
                    0.08
                ),
                windowMaterial
            );

        window1.position.set(
            -0.9,
            1.3,
            1.24
        );

        group.add(
            window1
        );


        /* WINDOW RIGHT */

        const window2 =
            window1.clone();

        window2.position.x =
            0.9;

        group.add(
            window2
        );


        scene.add(
            group
        );

        return group;
    }


    createHouse(
        0,
        -3.05,
        0.95
    );


    /* =====================================================
       SMALL ROCKS
    ===================================================== */

    function createRock(
        x,
        z,
        scale = 1
    ) {

        const rock =
            new THREE.Mesh(
                new THREE.DodecahedronGeometry(
                    0.35 * scale,
                    0
                ),
                new THREE.MeshStandardMaterial({
                    color: 0x7e8d82,
                    roughness: 1
                })
            );

        rock.position.set(
            x,
            0.3 * scale,
            z
        );

        rock.rotation.y =
            Math.random() * Math.PI;

        rock.castShadow = true;

        scene.add(
            rock
        );
    }


    createRock(
        3.8,
        -1.9,
        1
    );

    createRock(
        5.2,
        0.0,
        0.7
    );

    createRock(
        -3.0,
        -0.9,
        0.75
    );

    createRock(
        6.8,
        1.7,
        0.85
    );


    /* =====================================================
       TRAINER 3D
    ===================================================== */

    function createTrainer() {

        const group =
            new THREE.Group();

        group.position.y =
            0.05;


        /* BODY */

        const body =
            new THREE.Mesh(
                new THREE.CapsuleGeometry(
                    0.34,
                    0.75,
                    6,
                    12
                ),
                new THREE.MeshStandardMaterial({
                    color: 0x2563eb,
                    roughness: 0.8
                })
            );

        body.position.y =
            1.15;

        body.castShadow = true;

        group.add(
            body
        );


        /* HEAD */

        const head =
            new THREE.Mesh(
                new THREE.SphereGeometry(
                    0.37,
                    16,
                    12
                ),
                new THREE.MeshStandardMaterial({
                    color: 0xf0b27a,
                    roughness: 0.9
                })
            );

        head.position.y =
            2.05;

        head.castShadow = true;

        group.add(
            head
        );


        /* HAIR */

        const hair =
            new THREE.Mesh(
                new THREE.SphereGeometry(
                    0.4,
                    16,
                    10,
                    0,
                    Math.PI * 2,
                    0,
                    Math.PI * 0.55
                ),
                new THREE.MeshStandardMaterial({
                    color: 0x3b2417,
                    roughness: 1
                })
            );

        hair.position.y =
            2.16;

        hair.castShadow = true;

        group.add(
            hair
        );


        /* CAP */

        const cap =
            new THREE.Mesh(
                new THREE.CylinderGeometry(
                    0.44,
                    0.44,
                    0.12,
                    16
                ),
                new THREE.MeshStandardMaterial({
                    color: 0x1d4ed8,
                    roughness: 0.8
                })
            );

        cap.position.y =
            2.39;

        cap.castShadow = true;

        group.add(
            cap
        );


        /* CAP TOP */

        const capTop =
            new THREE.Mesh(
                new THREE.SphereGeometry(
                    0.31,
                    12,
                    8
                ),
                new THREE.MeshStandardMaterial({
                    color: 0x2563eb,
                    roughness: 0.8
                })
            );

        capTop.scale.y =
            0.45;

        capTop.position.y =
            2.44;

        capTop.castShadow = true;

        group.add(
            capTop
        );


        /* ARM LEFT */

        const armMaterial =
            new THREE.MeshStandardMaterial({
                color: 0x2563eb,
                roughness: 0.8
            });


        const armLeft =
            new THREE.Mesh(
                new THREE.CapsuleGeometry(
                    0.13,
                    0.5,
                    5,
                    8
                ),
                armMaterial
            );

        armLeft.position.set(
            -0.47,
            1.17,
            0
        );

        armLeft.rotation.z =
            -0.2;

        armLeft.castShadow = true;

        group.add(
            armLeft
        );


        /* ARM RIGHT */

        const armRight =
            armLeft.clone();

        armRight.position.x =
            0.47;

        armRight.rotation.z =
            0.2;

        group.add(
            armRight
        );


        /* LEG LEFT */

        const legMaterial =
            new THREE.MeshStandardMaterial({
                color: 0x1e3a8a,
                roughness: 0.9
            });


        const legLeft =
            new THREE.Mesh(
                new THREE.CapsuleGeometry(
                    0.14,
                    0.58,
                    5,
                    8
                ),
                legMaterial
            );

        legLeft.position.set(
            -0.19,
            0.48,
            0
        );

        legLeft.castShadow = true;

        group.add(
            legLeft
        );


        /* LEG RIGHT */

        const legRight =
            legLeft.clone();

        legRight.position.x =
            0.19;

        group.add(
            legRight
        );


        /* SHOES */

        const shoeMaterial =
            new THREE.MeshStandardMaterial({
                color: 0x111827,
                roughness: 0.9
            });


        const shoeLeft =
            new THREE.Mesh(
                new THREE.SphereGeometry(
                    0.18,
                    12,
                    8
                ),
                shoeMaterial
            );

        shoeLeft.scale.z =
            1.35;

        shoeLeft.position.set(
            -0.2,
            0.12,
            0.06
        );

        shoeLeft.castShadow = true;

        group.add(
            shoeLeft
        );


        const shoeRight =
            shoeLeft.clone();

        shoeRight.position.x =
            0.2;

        group.add(
            shoeRight
        );


        /* SHADOW */

        const shadow =
            new THREE.Mesh(
                new THREE.CircleGeometry(
                    0.7,
                    24
                ),
                new THREE.MeshBasicMaterial({
                    color: 0x000000,
                    transparent: true,
                    opacity: 0.2
                })
            );

        shadow.rotation.x =
            -Math.PI / 2;

        shadow.position.y =
            0.015;

        group.add(
            shadow
        );


        scene.add(
            group
        );

        return group;
    }


    trainer =
        createTrainer();


    /* =====================================================
       CONVERT HTML POSITION -> 3D POSITION
    ===================================================== */

    function getPlayerPixelPosition() {

        /*
         * offsetLeft/offsetTop là tọa độ pixel
         * thực tế của #player trong #gameWorld.
         *
         * Không dùng parseFloat(style.left)
         * vì style.left có thể là "50%".
         */

        const x =
            Number.isFinite(
                player2D.offsetLeft
            )
                ? player2D.offsetLeft
                : 450;

        const y =
            Number.isFinite(
                player2D.offsetTop
            )
                ? player2D.offsetTop
                : 250;

        return {
            x,
            y
        };
    }


    /* =====================================================
       MAP PIXEL -> WORLD
    ===================================================== */

    function pixelToWorld(
        x,
        y
    ) {

        const normalizedX =
            (x / MAP_WIDTH) - 0.5;

        const normalizedZ =
            (y / MAP_HEIGHT) - 0.5;

        return {

            x:
                normalizedX *
                WORLD_WIDTH,

            z:
                normalizedZ *
                WORLD_DEPTH

        };
    }


    /* =====================================================
       TRAINER FACING
    ===================================================== */

    function updateTrainerFacing(
        dx,
        dy
    ) {

        if (
            Math.abs(dx) <
                0.001
            &&
            Math.abs(dy) <
                0.001
        ) {
            return;
        }

        /*
         * 3D model hướng mặc định về phía +Z.
         */

        if (
            Math.abs(dx) >
            Math.abs(dy)
        ) {

            if (dx > 0) {

                trainer.rotation.y =
                    Math.PI / 2;

            } else {

                trainer.rotation.y =
                    -Math.PI / 2;

            }

        } else {

            if (dy > 0) {

                trainer.rotation.y =
                    Math.PI;

            } else {

                trainer.rotation.y =
                    0;

            }

        }
    }


    /* =====================================================
       WALK ANIMATION
    ===================================================== */

    function updateWalkAnimation(
        elapsed
    ) {

        if (
            previousPlayerX === null
            ||
            previousPlayerY === null
        ) {
            return;
        }

        const current =
            getPlayerPixelPosition();

        const moved =
            Math.abs(
                current.x -
                previousPlayerX
            ) > 0.2
            ||
            Math.abs(
                current.y -
                previousPlayerY
            ) > 0.2;


        if (moved) {

            const bob =
                Math.sin(
                    elapsed * 12
                ) * 0.06;

            trainer.position.y =
                0.05 + bob;

        } else {

            trainer.position.y =
                0.05;
        }
    }


    /* =====================================================
       SYNC TRAINER
    ===================================================== */

    function syncTrainer() {

        const current =
            getPlayerPixelPosition();

        const world =
            pixelToWorld(
                current.x,
                current.y
            );


        trainer.position.x =
            world.x;

        trainer.position.z =
            world.z;


        const dx =
            current.x -
            (previousPlayerX ?? current.x);

        const dy =
            current.y -
            (previousPlayerY ?? current.y);


        updateTrainerFacing(
            dx,
            dy
        );


        previousPlayerX =
            current.x;

        previousPlayerY =
            current.y;
    }


    /* =====================================================
       RESIZE
    ===================================================== */

    function resize() {

        const width =
            world3D.clientWidth;

        const height =
            world3D.clientHeight;


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


    /* =====================================================
       RESIZE OBSERVER
    ===================================================== */

    const resizeObserver =
        new ResizeObserver(
            () => {
                resize();
            }
        );

    resizeObserver.observe(
        world3D
    );


    /* =====================================================
       INITIAL POSITION
    ===================================================== */

    syncTrainer();

    resize();

    hide2D();


    /* =====================================================
       ANIMATION LOOP
    ===================================================== */

    function animate(
        time
    ) {

        animationFrame =
            requestAnimationFrame(
                animate
            );


        const elapsed =
            time * 0.001;


        const delta =
            lastTime === 0
                ? 0
                : elapsed - lastTime;


        lastTime =
            elapsed;


        syncTrainer();

        updateWalkAnimation(
            elapsed
        );


        /*
         * Nhẹ nhàng làm camera breathing
         */
        camera.position.y =
            13 +
            Math.sin(
                elapsed * 0.25
            ) * 0.08;


        camera.lookAt(
            0,
            0,
            0
        );


        renderer.render(
            scene,
            camera
        );
    }


    /* =====================================================
       START
    ===================================================== */

    try {

        animate(0);

        console.log(
            "Aetherlia 3D Map: Đã khởi tạo thành công."
        );

    } catch (error) {

        console.error(
            "Aetherlia 3D Map: Lỗi khi render.",
            error
        );

        cancelAnimationFrame(
            animationFrame
        );

        show2DFallback();
    }


    /* =====================================================
       DEBUG
    ===================================================== */

    window.Aetherlia3D = {

        scene,
        camera,
        renderer,
        trainer,

        getPlayerPosition:
            getPlayerPixelPosition,

        syncTrainer
    };

});