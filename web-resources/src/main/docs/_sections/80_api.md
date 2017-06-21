---
title: Series API Documentation
permalink: /api
data: swagger
---

{% assign swagger = site.data['swagger'] %}

{% for route in swagger.paths %}
<div class="swagger-paths">
  <h3 data-toc-skip class="swagger-path">{{ route[0] }}</h3>
  {% for method in route[1] %}
  <div class="swagger-method swagger-method-{{ method[0] }}">
    <h4 data-toc-skip class="swagger-method-title">
      <a href="#" class="swagger-method-link">
        <span class="swagger-method-name">{{ method[0] | upcase }}</span>
        {{ method[1].summary }}
      </a>
    </h4>
    <div class="swagger-method-details">
      {% if method[1].parameters %}
      <div class="swagger-method-description">
        {{ method[1].description | markdownify }}
      </div>
      <div class="swagger-parameters">
        <h5 data-toc-skip>Parameters</h5>
        <table class="table">
          <thead>
            <tr>
              <th>Name</th>
              <th>In</th>
              <th>Description</th>
              <th>Type</th>
            </tr>
          </thead>
          <tbody>
            {% for parameter in method[1].parameters %}
             {% if parameter.name == null %}
              {% assign ref = parameter['$ref'] | split:"/" | last %}
              <tr>
               <td>
                 {% if swagger.parameters[ref].required %}
                 <span class="swagger-parameter-required">
                 {% endif %}
                 <code>{{ swagger.parameters[ref].name }}</code>
                 {% if swagger.parameters[ref].required %}
                 </span>
                 {% endif %}
               </td>
               <td>{{ swagger.parameters[ref].in }}</td>
               <td>{{ swagger.parameters[ref].description | markdownify }}</td>
               <td>
                 {% if swagger.parameters[ref].type %}
                 {{ swagger.parameters[ref].type | capitalize }}
                 {% if swagger.parameters[ref].items %}
                 of {{ swagger.parameters[ref].items.type | capitalize }}
                 {% endif %}
                 {% else %}
                 String
                 {% endif %}
               </td>
              </tr>
              {% else %}
              <tr>
                <td>
                  {% if parameter.required %}
                  <span class="swagger-parameter-required">
                  {% endif %}
                  {{ parameter.name }}
                  {% if parameter.required %}
                  </span>
                  {% endif %}
                </td>
                <td>{{ parameter.in }}</td>
                <td>{{ parameter.description }}</td>
                <td>
                  {% if parameter.type %}
                  {{ parameter.type | capitalize }}
                  {% if parameter.items %}
                  of {{ parameter.items.type | capitalize }}
                  {% endif %}
                  {% else %}
                  String
                  {% endif %}
                </td>
              </tr>
             {% endif %}
            {% endfor %}
          </tbody>
        </table>
      </div>
      {% endif %}
      {% if method[1].responses %}
      <div class="swagger-response">
        <h5 data-toc-skip>Responses</h5>
        {% for response in method[1].responses %}
        <h6 data-toc-skip>
          <span class="swagger-response-code">{{ response[0] }}</span>
          {{ response[1].description }}
        </h6>
        {% for content_type in swagger.produces %}
          {% if response[1].examples[content_type] %}
            {% assign example = response[1].examples[content_type] %}
            {% if content_type contains 'json' %}
              {% highlight json %}{{ example }}{% endhighlight %}
            {% elsif content_type contains 'xml' %}
              {% highlight xml %}{{ example }}{% endhighlight %}
            {% else %}
              {% highlight http %}{{ example }}{% endhighlight %}
            {% endif %}
          {% endif %}
        {% endfor %}
        {% endfor %}
      </div>
      {% endif %}
    </div>
  </div>
  {% endfor %}
  
</div>
{% endfor %}
