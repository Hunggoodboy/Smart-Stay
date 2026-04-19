(function () {
    const roots = document.querySelectorAll('[data-select-root]');

    function closeAll(except) {
        roots.forEach((root) => {
            if (root === except) {
                return;
            }
            const menu = root.querySelector('[data-select-menu]');
            const trigger = root.querySelector('[data-select-trigger]');
            if (menu) {
                menu.classList.add('hidden');
            }
            if (trigger) {
                trigger.setAttribute('aria-expanded', 'false');
            }
        });
    }

    roots.forEach((root) => {
        const trigger = root.querySelector('[data-select-trigger]');
        const menu = root.querySelector('[data-select-menu]');
        const label = root.querySelector('[data-select-label]');
        const hiddenInput = root.querySelector('[data-select-value]');
        const options = root.querySelectorAll('[data-option-value]');

        if (!trigger || !menu || !label || !hiddenInput || options.length === 0) {
            return;
        }

        trigger.addEventListener('click', () => {
            const isOpen = !menu.classList.contains('hidden');
            closeAll(root);
            menu.classList.toggle('hidden', isOpen);
            trigger.setAttribute('aria-expanded', String(!isOpen));
        });

        options.forEach((option) => {
            option.addEventListener('click', () => {
                const value = option.getAttribute('data-option-value') || '';
                label.textContent = value;
                hiddenInput.value = value;

                options.forEach((item) => item.classList.remove('is-active'));
                option.classList.add('is-active');

                menu.classList.add('hidden');
                trigger.setAttribute('aria-expanded', 'false');
            });
        });
    });

    document.addEventListener('click', (event) => {
        const clickedInside = event.target.closest('[data-select-root]');
        if (!clickedInside) {
            closeAll(null);
        }
    });

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            closeAll(null);
        }
    });
})();
