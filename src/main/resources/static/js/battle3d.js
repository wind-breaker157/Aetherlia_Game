import * as THREE from "three";
import { GLTFLoader } from "three/addons/loaders/GLTFLoader.js";

// =====================================================
// AETHERLIA 3D BATTLE
// =====================================================

const MODEL_BASE_PATH = "/models/monsters/";

const MODEL_MAP = {
    spriglet: "spriglet",
    sprigrow: "sprigrow",
    sprigaia: "sprigaia",

    pyron: "pyron",
    pyroflare: "pyroflare",
    infernox: "infernox",

    aquaff: "aquaff",
    aquaflow: "aquaflow",
    aquarion: "aquarion"
};

// =====================================================
// THREE CORE
// =====================================================

let scene;
let camera;
let renderer;
let clock;

let stage;

let playerModel = null;
let wildModel = null;

let playerMixer = null;
let wildMixer = null;

let playerClips = [];
let wildClips = [];

let playerCurrentAction = null;
let wildCurrentAction = null;

let playerHomePosition = new THREE.Vector3(-3, 0, 0);
let wildHomePosition = new THREE.Vector3(3, 0, 0);

let playerOriginalScale = new THREE.Vector3(1, 1, 1);
let wildOriginalScale = new THREE.Vector3(1, 1, 1);

let cameraBasePosition = new THREE.Vector3(0, 4.5, 10);

let animationFrameId = null;

let captureRunning = false;
let effectRunning = false;

// =====================================================
// MONSTER CONFIG
// =====================================================

function normalizeMonsterKey(name) {

    if (!name) {
        return "spriglet";
    }

    let key = String(name)
        .trim()
        .toLowerCase()
        .normalize("NFD")
        .replace(/[\u0300-\u036f]/g, "")
        .replace(/[^a-z0-9]+/g, "-")
        .replace(/^-+|-+$/g, "");

    if (MODEL_MAP[key]) {
        return MODEL_MAP[key];
    }

    return key;
}

function getMonsterModelUrl(name) {

    const key = normalizeMonsterKey(name);

    return `${MODEL_BASE_PATH}${key}/${key}.glb`;
}

// =====================================================
// READ MONSTER FROM HTML
// =====================================================

function readBattleMonsterData() {

    const arena = document.querySelector(".battle-arena");

    let playerName = null;
    let wildName = null;

    if (arena) {
        playerName =
            arena.dataset.playerMonsterName ||
            arena.dataset.playerMonster ||
            null;

        wildName =
            arena.dataset.wildMonsterName ||
            arena.dataset.wildMonster ||
            null;
    }

    // Fallback: tìm phần tử mang data
    if (!playerName) {

        const playerElement =
            document.querySelector("[data-player-monster-name]");

        if (playerElement) {
            playerName = playerElement.dataset.playerMonsterName;
        }
    }

    if (!wildName) {

        const wildElement =
            document.querySelector("[data-wild-monster-name]");

        if (wildElement) {
            wildName = wildElement.dataset.wildMonsterName;
        }
    }

    // Fallback cuối cùng nếu trang vẫn chưa truyền tên
    if (!playerName) {
        playerName = "Spriglet";
    }

    if (!wildName) {
        wildName = "Spriglet";
    }

    return {
        playerName,
        wildName
    };
}

// =====================================================
// INIT
// =====================================================

function initBattle3D() {

    stage = document.getElementById("battle-3d-stage");

    if (!stage) {
        console.error("Không tìm thấy #battle-3d-stage.");
        return;
    }

    scene = new THREE.Scene();

    camera = new THREE.PerspectiveCamera(
        35,
        stage.clientWidth / stage.clientHeight,
        0.1,
        100
    );

    camera.position.copy(cameraBasePosition);
    camera.lookAt(0, 1.5, 0);

    renderer = new THREE.WebGLRenderer({
        antialias: true,
        alpha: true
    });

    renderer.setPixelRatio(
        Math.min(window.devicePixelRatio || 1, 2)
    );

    renderer.setSize(
        stage.clientWidth,
        stage.clientHeight
    );

    renderer.outputColorSpace = THREE.SRGBColorSpace;

    renderer.shadowMap.enabled = true;
    renderer.shadowMap.type = THREE.PCFSoftShadowMap;

    renderer.domElement.style.width = "100%";
    renderer.domElement.style.height = "100%";
    renderer.domElement.style.display = "block";

    stage.innerHTML = "";
    stage.appendChild(renderer.domElement);

    clock = new THREE.Clock();

    // =================================================
    // LIGHT
    // =================================================

    const ambientLight = new THREE.AmbientLight(
        0xffffff,
        2.2
    );

    scene.add(ambientLight);

    const keyLight = new THREE.DirectionalLight(
        0xffffff,
        4
    );

    keyLight.position.set(
        3,
        8,
        5
    );

    keyLight.castShadow = true;

    keyLight.shadow.mapSize.width = 2048;
    keyLight.shadow.mapSize.height = 2048;

    scene.add(keyLight);

    const fillLight = new THREE.DirectionalLight(
        0xffffff,
        2
    );

    fillLight.position.set(
        -5,
        4,
        3
    );

    scene.add(fillLight);

    const backLight = new THREE.PointLight(
        0xffffff,
        3,
        20
    );

    backLight.position.set(
        0,
        4,
        -4
    );

    scene.add(backLight);

    // =================================================
    // GROUND
    // =================================================

    const groundGeometry =
        new THREE.CircleGeometry(8, 64);

    const groundMaterial =
        new THREE.MeshStandardMaterial({
            color: 0x7fa36a,
            roughness: 1,
            metalness: 0
        });

    const ground =
        new THREE.Mesh(
            groundGeometry,
            groundMaterial
        );

    ground.rotation.x = -Math.PI / 2;

    ground.position.y = -0.02;

    ground.receiveShadow = true;

    scene.add(ground);

    // =================================================
    // BATTLE RINGS
    // =================================================

    createBattleRing(
        -3,
        0x4da3ff
    );

    createBattleRing(
        3,
        0xffb347
    );

    // =================================================
    // LOAD MODELS
    // =================================================

    const battleData = readBattleMonsterData();

    console.log(
        "PLAYER MONSTER:",
        battleData.playerName
    );

    console.log(
        "WILD MONSTER:",
        battleData.wildName
    );

    console.log(
        "PLAYER MODEL URL:",
        getMonsterModelUrl(
            battleData.playerName
        )
    );

    console.log(
        "WILD MODEL URL:",
        getMonsterModelUrl(
            battleData.wildName
        )
    );

    loadMonster(
        "PLAYER",
        battleData.playerName
    );

    loadMonster(
        "WILD",
        battleData.wildName
    );

    window.addEventListener(
        "resize",
        resize
    );

    animate();

    console.log(
        "AETHERLIA 3D Battle đã khởi tạo."
    );
}

// =====================================================
// BATTLE RING
// =====================================================

function createBattleRing(x, color) {

    const geometry =
        new THREE.RingGeometry(
            1.4,
            1.55,
            64
        );

    const material =
        new THREE.MeshBasicMaterial({
            color: color,
            transparent: true,
            opacity: 0.35,
            side: THREE.DoubleSide
        });

    const ring =
        new THREE.Mesh(
            geometry,
            material
        );

    ring.rotation.x = -Math.PI / 2;

    ring.position.set(
        x,
        0.01,
        0
    );

    scene.add(ring);
}

// =====================================================
// LOAD MONSTER
// =====================================================

function loadMonster(side, monsterName) {

    const loader = new GLTFLoader();

    const url =
        getMonsterModelUrl(monsterName);

    loader.load(
        url,

        function (gltf) {

            const model =
                gltf.scene;

            model.userData.battleSide = side;
            model.userData.monsterName = monsterName;

            // -----------------------------------------
            // POSITION
            // -----------------------------------------

            if (side === "PLAYER") {

				model.position.set(
				    -3,
				    0,
				    0
				);

				model.rotation.y = Math.PI / 2;
                playerModel = model;

                playerHomePosition.copy(
                    model.position
                );

                playerOriginalScale.copy(
                    model.scale
                );

                playerMixer =
                    new THREE.AnimationMixer(
                        model
                    );

                playerClips =
                    gltf.animations || [];

                console.log(
                    "PLAYER MODEL ĐÃ GÁN",
                    {
                        name: monsterName,
                        role: side,
                        x: model.position.x,
                        z: model.position.z
                    }
                );
            }

            // -----------------------------------------
            // WILD
            // -----------------------------------------

            if (side === "WILD") {

				model.position.set(
				    3,
				    0,
				    0
				);

				model.rotation.y =
				    -Math.PI / 2;

                wildModel = model;

                wildHomePosition.copy(
                    model.position
                );

                wildOriginalScale.copy(
                    model.scale
                );

                wildMixer =
                    new THREE.AnimationMixer(
                        model
                    );

                wildClips =
                    gltf.animations || [];

                console.log(
                    "WILD MODEL ĐÃ GÁN",
                    {
                        name: monsterName,
                        role: side,
                        x: model.position.x,
                        z: model.position.z
                    }
                );
            }

            // -----------------------------------------
            // MODEL SETTINGS
            // -----------------------------------------

            model.traverse(
                function (child) {

                    if (child.isMesh) {

                        child.castShadow = true;
                        child.receiveShadow = true;

                        if (
                            child.material &&
                            child.material.map
                        ) {

                            child.material.map.colorSpace =
                                THREE.SRGBColorSpace;
                        }
                    }
                }
            );

            scene.add(model);

            playIdle(side);

            console.log(
                `Đã tải Monster 3D: ${side}`
            );

            console.log(
                "Monster:",
                monsterName
            );

            console.log(
                "Animations:",
                (gltf.animations || []).map(
                    animation => animation.name
                )
            );
        },

        function (progress) {

            if (
                progress.total &&
                progress.total > 0
            ) {

                const percent =
                    (
                        progress.loaded /
                        progress.total
                    ) * 100;

                console.log(
                    `${side} loading: ${percent.toFixed(0)}%`
                );
            }
        },

        function (error) {

            console.error(
                `Không thể tải model ${side}:`,
                url
            );

            console.error(error);

            showModelError(
                side,
                monsterName
            );
        }
    );
}

// =====================================================
// MODEL ERROR
// =====================================================

function showModelError(
    side,
    monsterName
) {

    const message =
        document.createElement("div");

    message.className =
        "battle-3d-model-error";

    message.textContent =
        `${side}: chưa có model 3D cho ${monsterName}`;

    message.style.position = "absolute";
    message.style.left = "50%";
    message.style.top = side === "PLAYER"
        ? "75%"
        : "25%";

    message.style.transform =
        "translate(-50%, -50%)";

    message.style.padding =
        "8px 12px";

    message.style.borderRadius =
        "10px";

    message.style.background =
        "rgba(0,0,0,.65)";

    message.style.color =
        "#ffffff";

    message.style.fontSize =
        "12px";

    message.style.pointerEvents =
        "none";

    stage.appendChild(
        message
    );

    setTimeout(
        () => {

            message.remove();

        },
        5000
    );
}

// =====================================================
// ANIMATION HELPERS
// =====================================================

function findClip(
    clips,
    animationName
) {

    if (!clips || clips.length === 0) {
        return null;
    }

    const target =
        String(animationName)
            .toLowerCase();

    return clips.find(
        clip =>
            String(clip.name)
                .toLowerCase() === target
    ) || clips.find(
        clip =>
            String(clip.name)
                .toLowerCase()
                .includes(target)
    ) || null;
}

function playAnimation(
    side,
    animationName
) {

    const isPlayer =
        side === "PLAYER";

    const mixer =
        isPlayer
            ? playerMixer
            : wildMixer;

    const clips =
        isPlayer
            ? playerClips
            : wildClips;

    const currentAction =
        isPlayer
            ? playerCurrentAction
            : wildCurrentAction;

    if (!mixer || !clips) {
        return;
    }

    let clip =
        findClip(
            clips,
            animationName
        );

    // Nếu không có animation yêu cầu,
    // thử Idle.
    if (!clip) {

        clip =
            findClip(
                clips,
                "idle"
            );
    }

    // Nếu model không có Idle,
    // lấy animation đầu tiên.
    if (!clip && clips.length > 0) {

        clip = clips[0];
    }

    if (!clip) {
        return;
    }

    if (currentAction) {
        currentAction.stop();
    }

    const action =
        mixer.clipAction(
            clip
        );

    action.reset();

    if (
        animationName === "idle"
    ) {

        action.setLoop(
            THREE.LoopRepeat,
            Infinity
        );

        action.clampWhenFinished =
            false;

    } else {

        action.setLoop(
            THREE.LoopOnce,
            1
        );

        action.clampWhenFinished =
            true;

        mixer.addEventListener(
            "finished",
            function onFinished(event) {

                if (
                    event.action === action
                ) {

                    action.reset();

                    playIdle(
                        side
                    );

                    mixer.removeEventListener(
                        "finished",
                        onFinished
                    );
                }
            }
        );
    }

    action.fadeIn(0.08);

    action.play();

    if (isPlayer) {

        playerCurrentAction =
            action;

    } else {

        wildCurrentAction =
            action;
    }

    console.log(
        `PLAY ANIMATION: ${side} ${animationName}`
    );
}

function playIdle(side) {

    console.log(
        `PLAY IDLE: ${side}`
    );

    playAnimation(
        side,
        "idle"
    );
}

// =====================================================
// GENERIC ATTACK
// =====================================================

async function attack(
    attacker,
    target,
    damage,
    critical
) {

    if (!attacker || !target) {
        console.error(
            "Không thể attack vì thiếu model."
        );
        return;
    }

    const attackerSide =
        attacker.userData.battleSide;

    const targetSide =
        target.userData.battleSide;

    // -----------------------------------------------
    // ROLE SECURITY
    // -----------------------------------------------

    if (
        attackerSide === "PLAYER" &&
        targetSide !== "WILD"
    ) {

        console.error(
            "PLAYER chỉ được đánh WILD."
        );

        return;
    }

    if (
        attackerSide === "WILD" &&
        targetSide !== "PLAYER"
    ) {

        console.error(
            "WILD chỉ được đánh PLAYER."
        );

        return;
    }

    console.log(
        `3D ATTACK START: ${attackerSide} → ${targetSide}`,
        "damage:",
        damage
    );

    playAnimation(
        attackerSide,
        "attack"
    );

    // -----------------------------------------------
    // MOVE FORWARD
    // -----------------------------------------------

    const originalPosition =
        attacker.position.clone();

    const targetPosition =
        target.position.clone();

    const direction =
        new THREE.Vector3()
            .subVectors(
                targetPosition,
                originalPosition
            );

    direction.y = 0;

    if (direction.lengthSq() > 0) {
        direction.normalize();
    }

    const attackDistance = 0.9;

    const attackPosition =
        targetPosition.clone()
            .sub(
                direction.clone()
                    .multiplyScalar(
                        attackDistance
                    )
            );

    await moveModel(
        attacker,
        attackPosition,
        300
    );

    // -----------------------------------------------
    // HIT
    // -----------------------------------------------

    playAnimation(
        targetSide,
        "hit"
    );

    showDamageNumber(
        target,
        damage,
        critical
    );

    combatFeedback(
        target,
        critical
            ? "CRITICAL!"
            : "HIT"
    );

    shakeCamera(
        critical ? 0.16 : 0.07,
        critical ? 220 : 120
    );

    if (critical) {

        screenFlash(
            1
        );
    }

    await wait(
        critical ? 280 : 180
    );

    // -----------------------------------------------
    // RETURN HOME
    // -----------------------------------------------

    await moveModel(
        attacker,
        originalPosition,
        280
    );

    playIdle(
        attackerSide
    );

    console.log(
        `3D ATTACK END: ${attackerSide} → ${targetSide} damage: ${damage}`
    );
}

// =====================================================
// MOVE MODEL
// =====================================================

function moveModel(
    model,
    targetPosition,
    duration
) {

    return new Promise(
        resolve => {

            const start =
                model.position.clone();

            const startTime =
                performance.now();

            function update(time) {

                const elapsed =
                    time - startTime;

                let progress =
                    Math.min(
                        elapsed / duration,
                        1
                    );

                // smoothstep
                progress =
                    progress *
                    progress *
                    (3 - 2 * progress);

                model.position.lerpVectors(
                    start,
                    targetPosition,
                    progress
                );

                if (progress < 1) {

                    requestAnimationFrame(
                        update
                    );

                } else {

                    resolve();
                }
            }

            requestAnimationFrame(
                update
            );
        }
    );
}

// =====================================================
// PLAYER SKILL ATTACK
// =====================================================

async function playerSkillAttack(
    skillId,
    damage,
    critical
) {

    if (!playerModel || !wildModel) {

        console.error(
            "Thiếu PLAYER hoặc WILD model."
        );

        return;
    }

    console.log(
        "PLAYER ATTACK START"
    );

    console.log(
        "PLAYER MODEL:",
        playerModel.userData.battleSide
    );

    console.log(
        "WILD MODEL:",
        wildModel.userData.battleSide
    );

    console.log(
        "SKILL:",
        skillId
    );

    console.log(
        "DAMAGE:",
        damage
    );

    const skill =
        String(skillId || "")
            .toLowerCase();

    if (
        skill.includes("fire")
    ) {

        await fireBurst(
            playerModel,
            wildModel,
            damage,
            critical
        );

    } else if (
        skill.includes("water")
    ) {

        await waterShot(
            playerModel,
            wildModel,
            damage,
            critical
        );

    } else if (
        skill.includes("nature")
    ) {

        await natureStrike(
            playerModel,
            wildModel,
            damage,
            critical
        );

    } else {

        await attack(
            playerModel,
            wildModel,
            damage,
            critical
        );
    }

    console.log(
        "PLAYER ATTACK END"
    );
}

// =====================================================
// WILD ATTACK
// =====================================================

async function enemyAttack(
    damage,
    critical
) {

    if (!wildModel || !playerModel) {

        console.error(
            "Thiếu WILD hoặc PLAYER model."
        );

        return;
    }

    console.log(
        "WILD ATTACK"
    );

    console.log(
        "ATTACKER:",
        wildModel.userData.battleSide
    );

    console.log(
        "TARGET:",
        playerModel.userData.battleSide
    );

    console.log(
        "DAMAGE:",
        damage
    );

    await attack(
        wildModel,
        playerModel,
        damage,
        critical
    );

    console.log(
        "WILD COUNTER ATTACK END"
    );
}

// =====================================================
// FIRE
// =====================================================

async function fireBurst(
    attacker,
    target,
    damage,
    critical
) {

    if (!isValidAttackPair(
        attacker,
        target,
        "PLAYER",
        "WILD"
    )) {
        return;
    }

    console.log(
        "FIRE BURST: PLAYER → WILD"
    );

    await projectileAttack(
        attacker,
        target,
        damage,
        critical,
        0xff4d2e,
        "FIRE BURST"
    );
}

// =====================================================
// WATER
// =====================================================

async function waterShot(
    attacker,
    target,
    damage,
    critical
) {

    if (!isValidAttackPair(
        attacker,
        target,
        "PLAYER",
        "WILD"
    )) {
        return;
    }

    console.log(
        "WATER SHOT: PLAYER → WILD"
    );

    await projectileAttack(
        attacker,
        target,
        damage,
        critical,
        0x4da6ff,
        "WATER SHOT"
    );
}

// =====================================================
// NATURE
// =====================================================

async function natureStrike(
    attacker,
    target,
    damage,
    critical
) {

    if (!isValidAttackPair(
        attacker,
        target,
        "PLAYER",
        "WILD"
    )) {
        return;
    }

    console.log(
        "NATURE STRIKE: PLAYER → WILD"
    );

    await projectileAttack(
        attacker,
        target,
        damage,
        critical,
        0x7ed957,
        "NATURE STRIKE"
    );
}

// =====================================================
// PROJECTILE
// =====================================================

async function projectileAttack(
    attacker,
    target,
    damage,
    critical,
    color,
    label
) {

    playAnimation(
        attacker.userData.battleSide,
        "attack"
    );

    const start =
        attacker.position.clone();

    start.y += 1.1;

    const end =
        target.position.clone();

    end.y += 1.0;

    const geometry =
        new THREE.SphereGeometry(
            critical ? 0.24 : 0.15,
            24,
            24
        );

    const material =
        new THREE.MeshBasicMaterial({
            color: color
        });

    const projectile =
        new THREE.Mesh(
            geometry,
            material
        );

    projectile.position.copy(
        start
    );

    scene.add(
        projectile
    );

    const duration =
        critical ? 360 : 300;

    const startTime =
        performance.now();

    await new Promise(
        resolve => {

            function animateProjectile(
                time
            ) {

                const progress =
                    Math.min(
                        (time - startTime) /
                        duration,
                        1
                    );

                projectile.position.lerpVectors(
                    start,
                    end,
                    progress
                );

                // Arc
                projectile.position.y +=
                    Math.sin(
                        progress * Math.PI
                    ) * 0.6;

                projectile.scale.setScalar(
                    critical
                        ? 1.1 + progress * 0.4
                        : 1
                );

                if (progress < 1) {

                    requestAnimationFrame(
                        animateProjectile
                    );

                } else {

                    scene.remove(
                        projectile
                    );

                    geometry.dispose();
                    material.dispose();

                    resolve();
                }
            }

            requestAnimationFrame(
                animateProjectile
            );
        }
    );

    combatFeedback(
        target,
        label
    );

    showDamageNumber(
        target,
        damage,
        critical
    );

    if (critical) {

        screenFlash(
            1
        );

        shakeCamera(
            0.16,
            220
        );

    } else {

        shakeCamera(
            0.06,
            120
        );
    }

    playAnimation(
        target.userData.battleSide,
        "hit"
    );

    await wait(
        critical ? 280 : 180
    );

    playIdle(
        attacker.userData.battleSide
    );
}

// =====================================================
// VALIDATION
// =====================================================

function isValidAttackPair(
    attacker,
    target,
    attackerSide,
    targetSide
) {

    if (!attacker || !target) {
        return false;
    }

    const attackerRole =
        attacker.userData.battleSide;

    const targetRole =
        target.userData.battleSide;

    if (
        attackerRole !== attackerSide ||
        targetRole !== targetSide
    ) {

        console.error(
            "Sai role khi tấn công.",
            {
                attackerRole,
                targetRole
            }
        );

        return false;
    }

    return true;
}

// =====================================================
// DAMAGE NUMBER
// =====================================================

function showDamageNumber(
    target,
    damage,
    critical
) {

    if (!target || !camera || !stage) {
        return;
    }

    const worldPosition =
        target.position.clone();

    worldPosition.y += 2.1;

    const projected =
        worldPosition.project(
            camera
        );

    const x =
        (
            projected.x * 0.5 + 0.5
        ) * stage.clientWidth;

    const y =
        (
            -projected.y * 0.5 + 0.5
        ) * stage.clientHeight;

    const damageElement =
        document.createElement("div");

    damageElement.className =
        critical
            ? "battle-3d-damage critical"
            : "battle-3d-damage";

    damageElement.textContent =
        critical
            ? `-${damage} CRITICAL`
            : `-${damage}`;

    damageElement.style.position =
        "absolute";

    damageElement.style.left =
        `${x}px`;

    damageElement.style.top =
        `${y}px`;

    damageElement.style.transform =
        "translate(-50%, -50%) scale(1)";

    damageElement.style.fontWeight =
        "900";

    damageElement.style.fontSize =
        critical
            ? "28px"
            : "22px";

    damageElement.style.color =
        critical
            ? "#ffcc33"
            : "#ffffff";

    damageElement.style.textShadow =
        "0 2px 6px rgba(0,0,0,.9)";

    damageElement.style.zIndex =
        "100";

    damageElement.style.pointerEvents =
        "none";

    stage.appendChild(
        damageElement
    );

    requestAnimationFrame(
        () => {

            damageElement.style.transition =
                "transform .65s ease, opacity .65s ease";

            damageElement.style.transform =
                "translate(-50%, -130%) scale(1.15)";

            damageElement.style.opacity =
                "0";
        }
    );

    setTimeout(
        () => {

            damageElement.remove();

        },
        700
    );
}

// =====================================================
// COMBAT FEEDBACK
// =====================================================

function combatFeedback(
    target,
    text
) {

    if (!target || !camera || !stage) {
        return;
    }

    const position =
        target.position.clone();

    position.y += 2.7;

    const projected =
        position.project(
            camera
        );

    const x =
        (
            projected.x * 0.5 + 0.5
        ) * stage.clientWidth;

    const y =
        (
            -projected.y * 0.5 + 0.5
        ) * stage.clientHeight;

    const element =
        document.createElement("div");

    element.textContent =
        text;

    element.style.position =
        "absolute";

    element.style.left =
        `${x}px`;

    element.style.top =
        `${y}px`;

    element.style.transform =
        "translate(-50%, -50%)";

    element.style.padding =
        "6px 12px";

    element.style.borderRadius =
        "999px";

    element.style.background =
        "rgba(0,0,0,.65)";

    element.style.color =
        "#ffffff";

    element.style.fontWeight =
        "800";

    element.style.fontSize =
        "13px";

    element.style.zIndex =
        "101";

    element.style.pointerEvents =
        "none";

    element.style.transition =
        "transform .55s ease, opacity .55s ease";

    stage.appendChild(
        element
    );

    requestAnimationFrame(
        () => {

            element.style.transform =
                "translate(-50%, -130%)";

            element.style.opacity =
                "0";
        }
    );

    setTimeout(
        () => {

            element.remove();

        },
        600
    );
}

// =====================================================
// CAMERA SHAKE
// =====================================================

function shakeCamera(
    amount,
    duration
) {

    if (!camera) {
        return;
    }

    const original =
        camera.position.clone();

    const start =
        performance.now();

    function update(time) {

        const progress =
            Math.min(
                (time - start) /
                duration,
                1
            );

        if (progress < 1) {

            const strength =
                amount *
                (1 - progress);

            camera.position.x =
                original.x +
                (
                    Math.random() - 0.5
                ) *
                strength;

            camera.position.y =
                original.y +
                (
                    Math.random() - 0.5
                ) *
                strength;

            camera.position.z =
                original.z +
                (
                    Math.random() - 0.5
                ) *
                strength;

            requestAnimationFrame(
                update
            );

        } else {

            camera.position.copy(
                original
            );
        }
    }

    requestAnimationFrame(
        update
    );
}

// =====================================================
// SCREEN FLASH
// =====================================================

function screenFlash(
    intensity = 1
) {

    const element =
        document.createElement("div");

    element.style.position =
        "absolute";

    element.style.inset =
        "0";

    element.style.background =
        `rgba(255,255,255,${0.55 * intensity})`;

    element.style.zIndex =
        "120";

    element.style.pointerEvents =
        "none";

    element.style.opacity =
        "1";

    element.style.transition =
        "opacity .2s ease";

    stage.appendChild(
        element
    );

    requestAnimationFrame(
        () => {

            element.style.opacity =
                "0";
        }
    );

    setTimeout(
        () => {

            element.remove();

        },
        220
    );
}

// =====================================================
// FAINT
// =====================================================

async function faint(side) {

    const normalizedSide =
        String(side || "")
            .trim()
            .toUpperCase();

    let model = null;

    if (normalizedSide === "PLAYER") {
        model = playerModel;
    } else if (normalizedSide === "WILD") {
        model = wildModel;
    } else {
        console.error(
            "Faint nhận side không hợp lệ:",
            side
        );
        return;
    }

    if (!model) {
        console.error(
            `Không tìm thấy model cho side: ${normalizedSide}`
        );
        return;
    }

    console.log(
        `3D FAINT: ${normalizedSide}`
    );

    playAnimation(
        normalizedSide,
        "faint"
    );

    await wait(400);

    const startScale =
        model.scale.clone();

    const startY =
        model.position.y;

    const startRotationZ =
        model.rotation.z;

    const duration = 500;

    const startTime =
        performance.now();

    await new Promise(resolve => {

        function update(time) {

            const progress =
                Math.min(
                    (time - startTime) / duration,
                    1
                );

            model.rotation.z =
                startRotationZ +
                progress *
                Math.PI *
                0.5;

            model.position.y =
                startY -
                progress * 0.3;

            model.scale.lerpVectors(
                startScale,
                new THREE.Vector3(
                    0.05,
                    0.05,
                    0.05
                ),
                progress
            );

            if (progress < 1) {

                requestAnimationFrame(
                    update
                );

            } else {

                model.visible = false;

                resolve();
            }
        }

        requestAnimationFrame(
            update
        );
    });
}

// =====================================================
// VICTORY
// =====================================================

async function victory() {

    if (!playerModel) {
        return;
    }

    playerModel.visible = true;

    playerModel.position.copy(
        playerHomePosition
    );

    playerModel.scale.copy(
        playerOriginalScale
    );

    playAnimation(
        "PLAYER",
        "victory"
    );

    await moveModel(
        playerModel,
        new THREE.Vector3(
            playerHomePosition.x,
            playerHomePosition.y + 0.35,
            playerHomePosition.z
        ),
        180
    );

    await wait(
        250
    );

    await moveModel(
        playerModel,
        playerHomePosition,
        180
    );

    playIdle(
        "PLAYER"
    );
}

// =====================================================
// CAPTURE
// =====================================================

async function capture(
    orbType = "BASIC"
) {

    if (
        captureRunning ||
        !playerModel ||
        !wildModel
    ) {
        return false;
    }

    captureRunning = true;

    const orbColors = {
        BASIC: 0xffffff,
        GREAT: 0x4da6ff,
        ULTRA: 0xffcc33,
        MASTER: 0xaa66ff
    };

    const color =
        orbColors[
            String(orbType).toUpperCase()
        ] || 0xffffff;

    const start =
        playerModel.position.clone();

    start.y += 1.0;

    const end =
        wildModel.position.clone();

    end.y += 1.1;

    const orbGeometry =
        new THREE.SphereGeometry(
            0.22,
            24,
            24
        );

    const orbMaterial =
        new THREE.MeshStandardMaterial({
            color,
            emissive: color,
            emissiveIntensity: 1.2,
            metalness: 0.2,
            roughness: 0.35
        });

    const orb =
        new THREE.Mesh(
            orbGeometry,
            orbMaterial
        );

    orb.position.copy(
        start
    );

    scene.add(
        orb
    );

    // -----------------------------------------------
    // ORB -> WILD
    // -----------------------------------------------

    await animateObjectTo(
        orb,
        end,
        500
    );

    if (wildModel) {

        wildModel.visible = false;
    }

    combatFeedback(
        wildModel || playerModel,
        "CAPTURE"
    );

    await wait(
        300
    );

    // -----------------------------------------------
    // ORB SHAKE
    // -----------------------------------------------

    for (let i = 0; i < 3; i++) {

        orb.position.x += 0.16;

        await wait(
            140
        );

        orb.position.x -= 0.32;

        await wait(
            140
        );

        orb.position.x += 0.16;

        await wait(
            100
        );
    }

    // -----------------------------------------------
    // RESTORE IF FAILED
    // -----------------------------------------------

    if (wildModel) {

        wildModel.visible = true;

        wildModel.position.copy(
            wildHomePosition
        );

        wildModel.scale.copy(
            wildOriginalScale
        );

        wildModel.rotation.z = 0;

        playIdle(
            "WILD"
        );
    }

    scene.remove(
        orb
    );

    orbGeometry.dispose();
    orbMaterial.dispose();

    captureRunning = false;

    return true;
}

// =====================================================
// ANIMATE OBJECT
// =====================================================

function animateObjectTo(
    object,
    target,
    duration
) {

    return new Promise(
        resolve => {

            const start =
                object.position.clone();

            const startTime =
                performance.now();

            function update(
                time
            ) {

                const progress =
                    Math.min(
                        (time - startTime) /
                        duration,
                        1
                    );

                object.position.lerpVectors(
                    start,
                    target,
                    progress
                );

                if (progress < 1) {

                    requestAnimationFrame(
                        update
                    );

                } else {

                    resolve();
                }
            }

            requestAnimationFrame(
                update
            );
        }
    );
}

// =====================================================
// WAIT
// =====================================================

function wait(
    milliseconds
) {

    return new Promise(
        resolve =>
            setTimeout(
                resolve,
                milliseconds
            )
    );
}

// =====================================================
// GETTERS
// =====================================================

function getPlayerMonster() {
    return playerModel;
}

function getWildMonster() {
    return wildModel;
}

// =====================================================
// CAPTURE STATE
// =====================================================

function isCaptureRunning() {
    return captureRunning;
}

// =====================================================
// RENDER LOOP
// =====================================================

function animate() {

    animationFrameId =
        requestAnimationFrame(
            animate
        );

    const delta =
        clock.getDelta();

    if (playerMixer) {
        playerMixer.update(delta);
    }

    if (wildMixer) {
        wildMixer.update(delta);
    }

    renderer.render(
        scene,
        camera
    );
}

// =====================================================
// RESIZE
// =====================================================

function resize() {

    if (
        !stage ||
        !renderer ||
        !camera
    ) {
        return;
    }

    const width =
        stage.clientWidth;

    const height =
        stage.clientHeight;

    if (
        width <= 0 ||
        height <= 0
    ) {
        return;
    }

    camera.aspect =
        width / height;

    camera.updateProjectionMatrix();

    renderer.setSize(
        width,
        height
    );
}

// =====================================================
// EXPOSE API
// =====================================================

window.aetherliaBattle3D = {

    getPlayerMonster,

    getWildMonster,

    playerSkillAttack,

    enemyAttack,

    faint,

    victory,

    showDamageNumber,

    combatFeedback,

    shakeCamera,

    screenFlash,

    capture,

    isCaptureRunning
};

// =====================================================
// START
// =====================================================

if (
    document.readyState === "loading"
) {

    document.addEventListener(
        "DOMContentLoaded",
        initBattle3D
    );

} else {

    initBattle3D();
}