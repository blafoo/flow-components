import { render, html } from 'lit';

type RenderRoot = HTMLElement & { __litRenderer?: Renderer; _$litPart$?: any };

type ItemModel = { item: any; index: number };

type Renderer = (
  root: RenderRoot,
  rendererOwner: HTMLElement,
  model: ItemModel
) => void;

const _window = window as any;
_window.Vaadin = _window.Vaadin || {};

_window.Vaadin.setLitRenderer = (
  component: HTMLElement,
  rendererName: string,
  templateExpression: string,
  returnChannel: (name: string, itemKey: string, args: any[]) => void,
  clientCallables: string[],
  propertyNamespace: string
) => {
  // Dynamically created function that renders the templateExpression
  // inside the given root element using Lit
  const renderFunction = Function(`
    "use strict";

    const [render, html, returnChannel] = arguments;

    return (root, {item, index}, itemKey) => {
      ${clientCallables
        .map((clientCallable) => {
          // Map all the client-callables as inline functions so they can be accessed from the template (with @event-binding)
          return `
          const ${clientCallable} = (...args) => {
            returnChannel('${clientCallable}', itemKey, args[0] instanceof Event ? [] : [...args])
          }`;
        })
        .join('')}

      render(html\`${templateExpression}\`, root)
    }
  `)(render, html, returnChannel);

  const renderer: Renderer = (root, _, { index, item }) => {
    // Clean up the root element of any existing content
    // (and Lit's _$litPart$ property) from other renderers
    if (root.__litRenderer !== renderer) {
      root.innerHTML = '';
      delete root._$litPart$;
      root.__litRenderer = renderer;
    }

    // Map a new item that only includes the properties defined by
    // this specific LitRenderer instance. The renderer instance specific
    // "propertyNamespace" prefix is stripped from the property name at this point:
    //
    // item: { key: "19", d1baea7d-ecaa-4316-a1ae-0cf807672624_lastName: "Tyler" }
    // ->
    // mappedItem: { lastName: "Tyler" }
    const mappedItem = {};
    for (const key in item) {
      if (key.startsWith(propertyNamespace)) {
        mappedItem[key.replace(propertyNamespace, '')] = item[key];
      }
    }

    renderFunction(root, { index, item: mappedItem }, item.key);
  };

  (component as any)[rendererName] = renderer;
};
