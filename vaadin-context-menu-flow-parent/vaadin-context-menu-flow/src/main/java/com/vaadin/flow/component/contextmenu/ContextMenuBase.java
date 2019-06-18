/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.contextmenu;

import java.util.List;
import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.function.SerializableRunnable;
import com.vaadin.flow.shared.Registration;

/**
 * Base functionality for server-side components based on
 * {@code <vaadin-context-menu>}. Classes extending this should provide API for
 * adding items and handling events related to them. For basic example, see
 * {@link ContextMenu}.
 *
 * @param <C>
 *            the context-menu type
 * @param <I>
 *            the menu-item type
 * @param <S>
 *            the sub menu type
 *
 * @author Vaadin Ltd.
 */
@SuppressWarnings("serial")
@JsModule("flow-component-renderer.js")
@NpmPackage(value = "@vaadin/vaadin-list-box", version = "1.1.0")
@JsModule("@vaadin/vaadin-list-box/src/vaadin-list-box.js")
@JsModule("./contextMenuConnector-es6.js")

@HtmlImport("flow-component-renderer.html")
@HtmlImport("frontend://bower_components/vaadin-list-box/src/vaadin-list-box.html")
@JavaScript("frontend://contextMenuConnector.js")
public abstract class ContextMenuBase<C extends ContextMenuBase<C, I, S>, I extends MenuItemBase<C, I, S>, S extends SubMenuBase<C, I, S>>
        extends GeneratedVaadinContextMenu<C> implements HasComponents {

    private Component target;
    private MenuManager<C, I, S> menuManager;
    private MenuItemsArrayGenerator<I> menuItemsArrayGenerator;

    private String openOnEventName = "vaadin-contextmenu";
    private Registration targetBeforeOpenRegistration;
    private Registration targetAttachRegistration;

    private boolean autoAddedToTheUi;

    /**
     * Creates an empty context menu.
     */
    public ContextMenuBase() {
        // Workaround for: https://github.com/vaadin/flow/issues/3496
        getElement().setProperty("opened", false);

        // Don't open the overlay immediately with any event, let
        // contextMenuConnector.js make a server round-trip first.
        setOpenOn("none");

        getElement().addEventListener("opened-changed", event -> {
            if (autoAddedToTheUi && !isOpened()) {
                getElement().removeFromParent();
                autoAddedToTheUi = false;
            }
        });

        menuItemsArrayGenerator = new MenuItemsArrayGenerator<>(this);
        addAttachListener(event -> resetContent());
    }

    /**
     * Sets the target component for this context menu.
     * <p>
     * By default, the context menu can be opened with a right click or a long
     * touch on the target component.
     *
     * @param target
     *            the target component for this context menu, can be
     *            {@code null} to remove the target
     */
    public void setTarget(Component target) {
        if (getTarget() != null) {
            targetBeforeOpenRegistration.remove();
            targetAttachRegistration.remove();
            getTarget().getElement()
                    .callFunction("$contextMenuConnector.removeConnector");
        }

        this.target = target;
        getElement().getNode().runWhenAttached(
                ui -> ui.beforeClientResponse(this, context -> ui.getPage()
                        .executeJavaScript("$0.listenOn=$1", this, target)));

        if (target == null) {
            return;
        }

        // Target's JavaScript needs to be executed on each attach,
        // because Flow creates a new client-side element
        target.getUI().ifPresent(this::onTargetAttach);
        targetAttachRegistration = target
                .addAttachListener(e -> onTargetAttach(e.getUI()));

        // Server round-trip before opening the overlay
        targetBeforeOpenRegistration = target.getElement()
                .addEventListener("vaadin-context-menu-before-open", event -> {
                    beforeOpen();
                    target.getElement().callFunction(
                            "$contextMenuConnector.openMenu", getElement());
                });
    }

    /**
     * Gets the target component of this context menu, or {@code null} if it
     * doesn't have a target.
     *
     * @return the target component of this context menu
     * @see #setTarget(Component)
     */
    public Component getTarget() {
        return target;
    }

    /**
     * Determines the way for opening the context menu.
     * <p>
     * By default, the context menu can be opened with a right click or a long
     * touch on the target component.
     *
     * @param openOnClick
     *            if {@code true}, the context menu can be opened with left
     *            click only. Otherwise the context menu follows the default
     *            behavior.
     */
    public void setOpenOnClick(boolean openOnClick) {
        openOnEventName = openOnClick ? "click" : "vaadin-contextmenu";
        updateOpenOn();
    }

    /**
     * Gets whether the context menu can be opened via left click.
     * <p>
     * By default, this will return {@code false} and context menu can be opened
     * with a right click or a long touch on the target component.
     *
     * @return {@code true} if the context menu can be opened with left click
     *         only. Otherwise the context menu follows the default behavior.
     */
    public boolean isOpenOnClick() {
        return "click".equals(openOnEventName);
    }

    /**
     * Closes this context menu if it is currently open.
     */
    @Override
    public void close() {
        super.close();
    }

    /**
     * Adds a new item component with the given text content to the context menu
     * overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * highlightable {@link MenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link MenuItem}s, or if you just want to add some non-highlightable
     * components between the items, use the {@link #add(Component...)} method.
     *
     * @param text
     *            the text content for the created menu item
     * @return the created menu item
     * @see #add(Component...)
     */
    public I addItem(String text) {
        return getMenuManager().addItem(text);
    }

    /**
     * Adds a new item component with the given component to the context menu
     * overlay.
     * <p>
     * This is a convenience method for the use case where you have a list of
     * highlightable {@link MenuItem}s inside the overlay. If you want to
     * configure the contents of the overlay without wrapping them inside
     * {@link MenuItem}s, or if you just want to add some non-highlightable
     * components between the items, use the {@link #add(Component...)} method.
     *
     * @param component
     *            the component to add to the created menu item
     * @return the created menu item
     * @see #add(Component...)
     */
    public I addItem(Component component) {
        return getMenuManager().addItem(component);
    }

    /**
     * Adds the given components into the context menu overlay.
     * <p>
     * For the common use case of having a list of high-lightable items inside
     * the overlay, you can use the {@link #addItem(String)} convenience methods
     * instead.
     * <p>
     * The added elements in the DOM will not be children of the
     * {@code <vaadin-context-menu>} element, but will be inserted into an
     * overlay that is attached into the {@code <body>}.
     *
     * @param components
     *            the components to add
     * @see HasMenuItems#addItem(String, ComponentEventListener)
     * @see HasMenuItems#addItem(Component, ComponentEventListener)
     */
    @Override
    public void add(Component... components) {
        getMenuManager().add(components);
    }

    @Override
    public void remove(Component... components) {
        getMenuManager().remove(components);
    }

    /**
     * Removes all of the child components. This also removes all the items
     * added with {@link #addItem(String)} and its overload methods.
     */
    @Override
    public void removeAll() {
        getMenuManager().removeAll();
    }

    /**
     * Adds the given component into this context menu at the given index.
     * <p>
     * The added elements in the DOM will not be children of the
     * {@code <vaadin-context-menu>} element, but will be inserted into an
     * overlay that is attached into the {@code <body>}.
     *
     * @param index
     *            the index, where the component will be added
     * @param component
     *            the component to add
     * @see #add(Component...)
     */
    @Override
    public void addComponentAtIndex(int index, Component component) {
        getMenuManager().addComponentAtIndex(index, component);
    }

    /**
     * Gets the child components of this component. This includes components
     * added with {@link #add(Component...)} and the {@link MenuItem} components
     * created with {@link #addItem(String)} and its overload methods. This
     * doesn't include the components added to the sub menus of this context
     * menu.
     *
     * @return the child components of this component
     */
    @Override
    public Stream<Component> getChildren() {
        return getMenuManager().getChildren();
    }

    /**
     * Gets the items added to this component (the children of this component
     * that are instances of {@link MenuItem}). This doesn't include the
     * components added to the sub menus of this context menu.
     *
     * @return the {@link MenuItem} components in this context menu
     * @see #addItem(String)
     */
    public List<I> getItems() {
        return getMenuManager().getItems();
    }

    /**
     * Gets the open state from the context menu.
     *
     * @return the {@code opened} property from the context menu
     */
    public boolean isOpened() {
        return super.isOpenedBoolean();
    }

    /**
     * Adds a listener for the {@code opened-changed} events fired by the web
     * component.
     *
     * @param listener
     *            the listener to add
     * @return a Registration for removing the event listener
     */
    @Override
    public Registration addOpenedChangeListener(
            ComponentEventListener<OpenedChangeEvent<C>> listener) {
        return super.addOpenedChangeListener(listener);
    }

    /**
     * Gets the menu manager.
     *
     * @return the menu manager
     */
    protected MenuManager<C, I, S> getMenuManager() {
        if (menuManager == null) {
            menuManager = createMenuManager(this::resetContent);
        }
        return menuManager;
    }

    /**
     * Creates a menu manager instance which contains logic to control the menu
     * content.
     *
     * @param a
     *            callback to reset the menu content
     * @return a new menu manager instance
     */
    protected abstract MenuManager<C, I, S> createMenuManager(
            SerializableRunnable contentReset);

    private void resetContent() {
        menuItemsArrayGenerator.generate();
    }

    private void onTargetAttach(UI ui) {
        ui.getInternals().addComponentDependencies(ContextMenu.class);
        ui.getPage().executeJs(
                "window.Vaadin.Flow.contextMenuConnector.init($0)", target);
        updateOpenOn();
    }

    private void updateOpenOn() {
        if (target != null) {
            target.getElement().callFunction(
                    "$contextMenuConnector.updateOpenOn", openOnEventName);
        }
    }

    private void beforeOpen() {
        if (getElement().getNode().getParent() == null) {
            UI ui = getCurrentUI();
            ui.beforeClientResponse(ui, context -> {
                ui.add(this);
                autoAddedToTheUi = true;
            });
        }
    }

    private UI getCurrentUI() {
        UI ui = UI.getCurrent();
        if (ui == null) {
            throw new IllegalStateException("UI instance is not available. "
                    + "It means that you are calling this method "
                    + "out of a normal workflow where it's always implicitly set. "
                    + "That may happen if you call the method from the custom thread without "
                    + "'UI::access' or from tests without proper initialization.");
        }
        return ui;
    }
}
