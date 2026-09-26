// =====================================================
// AETHERLIA BATTLE JS
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    () => {
        initBattle();
    }
);

// =====================================================
// INIT
// =====================================================

function initBattle() {

    // =================================================
    // ATTACK FORMS
    // =================================================

    const attackForms =
        document.querySelectorAll(
            'form[action$="/battle/attack"]'
        );

    attackForms.forEach(
        (form) => {

            form.addEventListener(
                "submit",
                (event) => {

                    event.preventDefault();

                    performAttack(
                        form
                    );
                }
            );
        }
    );

    // =================================================
    // CAPTURE FORMS
    // =================================================

    const captureForms =
        document.querySelectorAll(
            'form[action$="/battle/capture"]'
        );

    captureForms.forEach(
        (form) => {

            form.addEventListener(
                "submit",
                (event) => {

                    event.preventDefault();

                    performCapture(
                        form
                    );
                }
            );
        }
    );
}

// =====================================================
// PERFORM ATTACK
// =====================================================

async function performAttack(
    form
) {

    const attackButtons =
        document.querySelectorAll(
            'form[action$="/battle/attack"] button'
        );

    setButtonsDisabled(
        attackButtons,
        true
    );

    const captureButtons =
        document.querySelectorAll(
            'form[action$="/battle/capture"] button'
        );

    setButtonsDisabled(
        captureButtons,
        true
    );

    const arena =
        document.querySelector(
            ".battle-arena"
        );

    if (arena) {

        arena.classList.add(
            "attack-running"
        );
    }

    // =================================================
    // FORM DATA
    // =================================================

    const formData =
        new FormData(
            form
        );

    const skillId =
        formData.get(
            "skillId"
        );

    if (!skillId) {

        showBattleMessage(
            "Không tìm thấy skillId."
        );

        resetAttackState(
            attackButtons,
            captureButtons,
            arena
        );

        return;
    }

    // =================================================
    // API
    // =================================================

    try {

        const response =
            await fetch(
                "/battle/api/attack",
                {
                    method: "POST",
                    body: formData,
                    headers: {
                        "X-Requested-With":
                            "XMLHttpRequest"
                    }
                }
            );

        if (!response.ok) {

            throw new Error(
                "HTTP " +
                response.status
            );
        }

        const data =
            await response.json();

        console.log(
            "Battle API:",
            data
        );

        if (!data.success) {

            throw new Error(
                data.message ||
                "Không thể xử lý lượt đánh."
            );
        }

        // =================================================
        // PLAY TURN
        // =================================================

        await playBattleTurn(
            data,
            skillId
        );

    } catch (error) {

        console.error(
            "Battle API error:",
            error
        );

        showBattleMessage(
            error.message ||
            "Không thể xử lý lượt đánh."
        );

        resetAttackState(
            attackButtons,
            captureButtons,
            arena
        );
    }
}

// =====================================================
// PLAY BATTLE TURN
// =====================================================
//
// SERVER:
//
// PLAYER -> WILD
// WILD   -> PLAYER
//
// FRONTEND:
//
// PLAYER -> WILD
// WILD   -> PLAYER
//
// Không dùng playerFirst.
// =====================================================

async function playBattleTurn(
    data,
    skillId
) {

    const battle3D =
        window.aetherliaBattle3D;

    // =================================================
    // KHÔNG CÓ 3D
    // =================================================

    if (!battle3D) {

        updateHpBars(
            data
        );

        finishBattle(
            data
        );

        return;
    }

    // =================================================
    // KIỂM TRA API
    // =================================================

    if (
        typeof battle3D.playerSkillAttack !==
        "function"
    ) {

        console.error(
            "playerSkillAttack() không tồn tại."
        );

        updateHpBars(
            data
        );

        finishBattle(
            data
        );

        return;
    }

    if (
        typeof battle3D.enemyAttack !==
        "function"
    ) {

        console.error(
            "enemyAttack() không tồn tại."
        );

        updateHpBars(
            data
        );

        finishBattle(
            data
        );

        return;
    }

    // =================================================
    // MODEL
    // =================================================

    const player =
        typeof battle3D.getPlayerMonster ===
        "function"
            ? battle3D.getPlayerMonster()
            : null;

    const wild =
        typeof battle3D.getWildMonster ===
        "function"
            ? battle3D.getWildMonster()
            : null;

    if (
        !player ||
        !wild
    ) {

        console.warn(
            "3D Monster chưa load."
        );

        updateHpBars(
            data
        );

        finishBattle(
            data
        );

        return;
    }

    // =================================================
    // DEBUG ROLE
    // =================================================

    console.log(
        "================================="
    );

    console.log(
        "BATTLE TURN"
    );

    console.log(
        "PLAYER MODEL:",
        player.userData?.battleSide
    );

    console.log(
        "WILD MODEL:",
        wild.userData?.battleSide
    );

    console.log(
        "playerDamage:",
        data.playerDamage
    );

    console.log(
        "enemyDamage:",
        data.enemyDamage
    );

    console.log(
        "wildHp:",
        data.wildHp
    );

    console.log(
        "playerHp:",
        data.playerHp
    );

    console.log(
        "================================="
    );

    // =================================================
    // 1. PLAYER ATTACK
    // =================================================
    //
    // KHÔNG:
    //
    // skillAttack(player, wild, ...)
    //
    // MÀ:
    //
    // playerSkillAttack(...)
    //
    // playerSkillAttack() tự khóa:
    //
    // playerModel -> wildModel
    // =================================================

    if (
        Number(
            data.playerDamage || 0
        ) > 0
    ) {

        console.log(
            "PLAYER ATTACK START"
        );

        await battle3D.playerSkillAttack(
            skillId,
            Number(
                data.playerDamage || 0
            ),
            Boolean(
                data.playerCritical
            )
        );

        console.log(
            "PLAYER ATTACK END"
        );

        // =================================================
        // EFFECT TRÊN WILD
        // =================================================

        await showAttackFeedback(
            data,
            "player",
            wild
        );

        // =================================================
        // WILD HP
        // =================================================

        animateHpBar(
            "wild",
            data.wildHpBefore,
            data.wildHp,
            data.wildMaxHp
        );
    }

    // =================================================
    // WILD BỊ HẠ
    // =================================================

    if (
        Number(
            data.wildHp || 0
        ) <= 0
    ) {

        console.log(
            "WILD DEFEATED"
        );

        await battle3D.faint(
            wild
        );

        battle3D.victory(
            player
        );

        showBattleMessage(
            data.message
        );

        finishBattle(
            data
        );

        return;
    }

    // =================================================
    // CHỜ
    // =================================================

    await wait(
        300
    );

    // =================================================
    // 2. WILD COUNTER ATTACK
    // =================================================
    //
    // enemyAttack() tự khóa:
    //
    // wildModel -> playerModel
    // =================================================

    if (
        Number(
            data.enemyDamage || 0
        ) > 0
    ) {

        console.log(
            "WILD COUNTER ATTACK START"
        );

        await battle3D.enemyAttack(
            Number(
                data.enemyDamage || 0
            ),
            Boolean(
                data.enemyCritical
            )
        );

        console.log(
            "WILD COUNTER ATTACK END"
        );

        // =================================================
        // EFFECT TRÊN PLAYER
        // =================================================

        await showAttackFeedback(
            data,
            "enemy",
            player
        );

        // =================================================
        // PLAYER HP
        // =================================================

        animateHpBar(
            "player",
            data.playerHpBefore,
            data.playerHp,
            data.playerMaxHp
        );
    }

    // =================================================
    // PLAYER BỊ HẠ
    // =================================================

    if (
        Number(
            data.playerHp || 0
        ) <= 0
    ) {

        console.log(
            "PLAYER DEFEATED"
        );

        await battle3D.faint(
            player
        );

        showBattleMessage(
            data.message
        );

        finishBattle(
            data
        );

        return;
    }

    // =================================================
    // BATTLE CONTINUE
    // =================================================

    showBattleMessage(
        data.message
    );

    finishBattle(
        data
    );
}

// =====================================================
// ATTACK FEEDBACK
// =====================================================

async function showAttackFeedback(
    data,
    attacker,
    target
) {

    const battle3D =
        window.aetherliaBattle3D;

    // =================================================
    // CRITICAL
    // =================================================

    const critical =
        attacker === "player"
            ? Boolean(
                data.playerCritical
            )
            : Boolean(
                data.enemyCritical
            );

    // =================================================
    // TYPE
    // =================================================

    const multiplier =
        attacker === "player"
            ? Number(
                data.playerTypeMultiplier ??
                1
            )
            : Number(
                data.enemyTypeMultiplier ??
                1
            );

    // =================================================
    // 3D FEEDBACK
    // =================================================

    if (
        battle3D &&
        typeof battle3D.combatFeedback ===
        "function"
    ) {

        battle3D.combatFeedback(
            target,
            multiplier,
            critical
        );
    }

    // =================================================
    // CRITICAL
    // =================================================

    if (critical) {

        showCombatAnnouncement(
            "CRITICAL HIT!",
            "critical"
        );

        await wait(
            450
        );
    }

    // =================================================
    // SUPER
    // =================================================

    if (
        multiplier > 1
    ) {

        showCombatAnnouncement(
            "SUPER EFFECTIVE!",
            "super"
        );

        await wait(
            550
        );

        return;
    }

    // =================================================
    // WEAK
    // =================================================

    if (
        multiplier > 0 &&
        multiplier < 1
    ) {

        showCombatAnnouncement(
            "NOT VERY EFFECTIVE...",
            "weak"
        );

        await wait(
            550
        );
    }
}

// =====================================================
// COMBAT ANNOUNCEMENT
// =====================================================

function showCombatAnnouncement(
    text,
    type
) {

    const stage =
        document.getElementById(
            "battle-3d-stage"
        );

    if (!stage) {
        return;
    }

    const old =
        stage.querySelector(
            ".battle-combat-announcement"
        );

    if (old) {
        old.remove();
    }

    const element =
        document.createElement(
            "div"
        );

    element.className =
        "battle-combat-announcement " +
        type;

    element.textContent =
        text;

    stage.appendChild(
        element
    );

    setTimeout(
        () => {

            if (
                element.parentNode
            ) {

                element.remove();
            }

        },
        900
    );
}

// =====================================================
// UPDATE HP BARS
// =====================================================

function updateHpBars(
    data
) {

    updateHpText(
        "player",
        data.playerHp,
        data.playerMaxHp
    );

    updateHpText(
        "wild",
        data.wildHp,
        data.wildMaxHp
    );
}

// =====================================================
// HP ANIMATION
// =====================================================

function animateHpBar(
    side,
    fromHp,
    toHp,
    maxHp
) {

    const textElement =
        document.getElementById(
            side === "player"
                ? "player-hp-text"
                : "wild-hp-text"
        );

    const fillElement =
        document.getElementById(
            side === "player"
                ? "player-hp-fill"
                : "wild-hp-fill"
        );

    if (
        !textElement ||
        !fillElement
    ) {

        return;
    }

    const start =
        Number(
            fromHp ?? 0
        );

    const end =
        Number(
            toHp ?? 0
        );

    const maximum =
        Number(
            maxHp ?? 0
        );

    const startTime =
        performance.now();

    const duration =
        450;

    function step(
        timestamp
    ) {

        const progress =
            Math.min(
                1,
                (
                    timestamp -
                    startTime
                ) /
                duration
            );

        const current =
            Math.round(
                start +
                (
                    end -
                    start
                ) *
                progress
            );

        textElement.textContent =
            current;

        const percent =
            maximum > 0
                ? (
                    current /
                    maximum
                ) *
                100
                : 0;

        fillElement.style.width =
            Math.max(
                0,
                Math.min(
                    100,
                    percent
                )
            ) +
            "%";

        if (
            progress < 1
        ) {

            requestAnimationFrame(
                step
            );

        } else {

            textElement.textContent =
                end;

            const finalPercent =
                maximum > 0
                    ? (
                        end /
                        maximum
                    ) *
                    100
                    : 0;

            fillElement.style.width =
                Math.max(
                    0,
                    Math.min(
                        100,
                        finalPercent
                    )
                ) +
                "%";
        }
    }

    requestAnimationFrame(
        step
    );
}

// =====================================================
// UPDATE HP TEXT
// =====================================================

function updateHpText(
    side,
    hp,
    maxHp
) {

    const textElement =
        document.getElementById(
            side === "player"
                ? "player-hp-text"
                : "wild-hp-text"
        );

    const fillElement =
        document.getElementById(
            side === "player"
                ? "player-hp-fill"
                : "wild-hp-fill"
        );

    const currentHp =
        Number(
            hp ?? 0
        );

    const maximum =
        Number(
            maxHp ?? 0
        );

    if (textElement) {

        textElement.textContent =
            currentHp;
    }

    if (
        fillElement &&
        maximum > 0
    ) {

        const percent =
            (
                currentHp /
                maximum
            ) *
            100;

        fillElement.style.width =
            Math.max(
                0,
                Math.min(
                    100,
                    percent
                )
            ) +
            "%";
    }
}

// =====================================================
// BATTLE MESSAGE
// =====================================================

function showBattleMessage(
    message
) {

    const element =
        document.querySelector(
            ".battle-message"
        );

    if (element) {

        element.textContent =
            message || "";
    }
}

// =====================================================
// FINISH BATTLE
// =====================================================

function finishBattle(
    data
) {

    showBattleMessage(
        data.message
    );

    const attackButtons =
        document.querySelectorAll(
            'form[action$="/battle/attack"] button'
        );

    const captureButtons =
        document.querySelectorAll(
            'form[action$="/battle/capture"] button'
        );

    const arena =
        document.querySelector(
            ".battle-arena"
        );

    // =================================================
    // END
    // =================================================

    if (
        data.victory ||
        data.defeat
    ) {

        setButtonsDisabled(
            attackButtons,
            true
        );

        setButtonsDisabled(
            captureButtons,
            true
        );

        if (arena) {

            arena.classList.remove(
                "attack-running"
            );
        }

        return;
    }

    // =================================================
    // CONTINUE
    // =================================================

    setButtonsDisabled(
        attackButtons,
        false
    );

    setButtonsDisabled(
        captureButtons,
        false
    );

    if (arena) {

        arena.classList.remove(
            "attack-running"
        );
    }
}

// =====================================================
// RESET ATTACK
// =====================================================

function resetAttackState(
    attackButtons,
    captureButtons,
    arena
) {

    setButtonsDisabled(
        attackButtons,
        false
    );

    setButtonsDisabled(
        captureButtons,
        false
    );

    if (arena) {

        arena.classList.remove(
            "attack-running"
        );
    }
}

// =====================================================
// CAPTURE
// =====================================================

async function performCapture(
    form
) {

    const captureButtons =
        document.querySelectorAll(
            'form[action$="/battle/capture"] button'
        );

    const attackButtons =
        document.querySelectorAll(
            'form[action$="/battle/attack"] button'
        );

    setButtonsDisabled(
        captureButtons,
        true
    );

    setButtonsDisabled(
        attackButtons,
        true
    );

    const arena =
        document.querySelector(
            ".battle-arena"
        );

    if (arena) {

        arena.classList.add(
            "capture-running"
        );
    }

    const formData =
        new FormData(
            form
        );

    const orbType =
        formData.get(
            "orbType"
        );

    if (!orbType) {

        showBattleMessage(
            "Không tìm thấy loại Orb."
        );

        resetCaptureState(
            captureButtons,
            attackButtons,
            arena
        );

        return;
    }

    try {

        // =================================================
        // API
        // =================================================

        const response =
            await fetch(
                "/battle/api/capture",
                {
                    method: "POST",
                    body: formData,
                    headers: {
                        "X-Requested-With":
                            "XMLHttpRequest"
                    }
                }
            );

        if (!response.ok) {

            throw new Error(
                "HTTP " +
                response.status
            );
        }

        const data =
            await response.json();

        console.log(
            "Capture API:",
            data
        );

        if (!data.success) {

            throw new Error(
                data.message ||
                "Không thể thực hiện bắt quái."
            );
        }

        const captureSuccess =
            Boolean(
                data.captureSuccess
            );

        const shakes =
            Number(
                data.shakes || 3
            );

        showBattleMessage(
            data.message
        );

        // =================================================
        // 3D
        // =================================================

        const battle3D =
            window.aetherliaBattle3D;

        if (
            battle3D &&
            typeof battle3D.capture ===
            "function"
        ) {

            await battle3D.capture(
                orbType,
                captureSuccess,
                shakes
            );
        }

        // =================================================
        // CAPTURE SUCCESS
        // =================================================

        if (
            captureSuccess
        ) {

            showBattleMessage(
                data.message
            );

            await wait(
                300
            );

            window.location.href =
                data.redirectUrl ||
                "/my-monsters";

            return;
        }

        // =================================================
        // CAPTURE FAILED
        // =================================================
        //
        // Backend:
        //
        // WILD -> PLAYER
        //
        // Frontend:
        //
        // enemyAttack()
        // =================================================

        if (
            Number(
                data.enemyDamage || 0
            ) > 0
        ) {

            const player =
                battle3D &&
                typeof battle3D.getPlayerMonster ===
                "function"
                    ? battle3D.getPlayerMonster()
                    : null;

            if (
                battle3D &&
                player &&
                typeof battle3D.enemyAttack ===
                "function"
            ) {

                await wait(
                    250
                );

                await battle3D.enemyAttack(
                    Number(
                        data.enemyDamage || 0
                    ),
                    Boolean(
                        data.enemyCritical
                    )
                );

                await showAttackFeedback(
                    data,
                    "enemy",
                    player
                );
            }

            animateHpBar(
                "player",
                data.playerHpBefore,
                data.playerHp,
                data.playerMaxHp
            );
        }

        showBattleMessage(
            data.message
        );

        // =================================================
        // PLAYER DEFEATED
        // =================================================

        if (
            Number(
                data.playerHp || 0
            ) <= 0 ||
            data.defeat
        ) {

            if (
                battle3D &&
                typeof battle3D.getPlayerMonster ===
                "function"
            ) {

                const player =
                    battle3D.getPlayerMonster();

                if (player) {

                    await battle3D.faint(
                        player
                    );
                }
            }

            resetCaptureState(
                captureButtons,
                attackButtons,
                arena
            );

            return;
        }

        // =================================================
        // CONTINUE
        // =================================================

        resetCaptureState(
            captureButtons,
            attackButtons,
            arena
        );

    } catch (error) {

        console.error(
            "Capture API error:",
            error
        );

        showBattleMessage(
            error.message ||
            "Không thể thực hiện bắt quái."
        );

        resetCaptureState(
            captureButtons,
            attackButtons,
            arena
        );
    }
}

// =====================================================
// RESET CAPTURE
// =====================================================

function resetCaptureState(
    captureButtons,
    attackButtons,
    arena
) {

    setButtonsDisabled(
        captureButtons,
        false
    );

    setButtonsDisabled(
        attackButtons,
        false
    );

    if (arena) {

        arena.classList.remove(
            "capture-running"
        );
    }
}

// =====================================================
// BUTTON STATE
// =====================================================

function setButtonsDisabled(
    buttons,
    disabled
) {

    buttons.forEach(
        (button) => {

            button.disabled =
                disabled;
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
        (resolve) => {

            setTimeout(
                resolve,
                Math.max(
                    0,
                    milliseconds || 0
                )
            );
        }
    );
}